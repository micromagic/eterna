/*
 * Copyright 2015 xinjunli (micromagic@sina.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package self.micromagic.eterna.digester2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.IntegerRef;

/**
 * 配置资源及其位置描述对象.
 */
public interface ConfigResource
{
	/**
	 * 资源类型: 文件.
	 */
	int RES_TYPE_FILE = 1;
	/**
	 * 资源类型: 目录.
	 */
	int RES_TYPE_DIR = 2;

	/**
	 * 创建一个当前类型的配置资源.
	 *
	 * @param config     配置资源的位置描述信息
	 * @param container  当前的工厂容器
	 */
	ConfigResource create(String config, FactoryContainer container);

	/**
	 * 获取配置资源的位置描述信息.
	 */
	String getConfig();

	/**
	 * 获取资源的类型.
	 *
	 * @see #RES_TYPE_FILE
	 * @see #RES_TYPE_DIR
	 */
	int getType();

	/**
	 * 获取最后更新的时间(毫秒数).
	 * 如果无法获取最后更新时间则返回-1.
	 */
	long getLastModified();

	/**
	 * 获取资源的定义信息.
	 */
	String getURI();

	/**
	 * 获取资源的名称.
	 */
	String getName();

	/**
	 * 如果资源类型是文件, 可通过此方法获取数据流.
	 * 如果资源不存在则返回null.
	 */
	InputStream getAsStream();

	/**
	 * 如果资源类型是目录, 可通过此方法获取目录下的所有资源.
	 * 如果目录不存在则返回null.
	 *
	 * @param recursive  是否需要递归获取子目录
	 */
	ConfigResource[] listResources(boolean recursive);

	/**
	 * 根据给出的路径获取新的配置资源的对象.
	 * 如果不存在则返回null.
	 */
	ConfigResource getResource(String path);

}

/**
 * 公共的资源处理方法.
 */
class AbstractResource
{
	protected String config;
	protected FactoryContainer container;
	protected File resFile;
	protected int type = ConfigResource.RES_TYPE_FILE;

	/**
	 * 初始化各个变量.
	 */
	protected void init(String config, FactoryContainer container, URL url)
	{
		this.config = config;
		if (config != null && config.endsWith("/"))
		{
			this.type = ConfigResource.RES_TYPE_DIR;
		}
		this.container = container;
		if (url != null)
		{
			if ("file".equals(url.getProtocol()))
			{
				File f = new File(url.getFile());
				if (f.isFile())
				{
					this.resFile = f;
				}
			}
		}
	}

	public String getConfig()
	{
		return this.config;
	}

	public int getType()
	{
		return this.type;
	}

	public String getName()
	{
		if (!StringTool.isEmpty(this.config))
		{
			int end = this.config.length();
			if (this.config.charAt(end - 1) == '/')
			{
				end--;
			}
			int begin = this.config.lastIndexOf('/', end - 1);
			if (begin != -1 && begin < end)
			{
				return this.config.substring(begin + 1, end);
			}
		}
		return "";
	}

	public long getLastModified()
	{
		if (this.resFile != null)
		{
			return this.resFile.lastModified();
		}
		return -1L;
	}

	/**
	 * 检查给出的路径是否包含前缀标识.
	 * 如果有则创建一个ConfigResource, 否则返回null.
	 */
	protected ConfigResource checkFlag(String path)
	{
		int index = path.indexOf(':');
		if (index != -1)
		{
			return ContainerManager.createResource(path, this.container);
		}
		return null;
	}


	/**
	 * 对一个路径进行解析, 将路径中的各个部分分解成数组返回.
	 *
	 * @param path    需要解析的路径
	 * @param pCount  出参, 路径需要向上递归的层数, 如: ../../test
	 *                如果是绝对路径则值为-1
	 * @param err     出参, 如果路径有错误, 可从这里获取出错信息
	 * @return  如果解析成功的话则返回解析完的数组, 否则返回null
	 */
	protected static String[] parsePath(String path, IntegerRef pCount)
	{
		if (StringTool.isEmpty(path) || StringTool.isEmpty(path = path.trim()))
		{
			return StringTool.EMPTY_STRING_ARRAY;
		}
		boolean absolutePath = path.charAt(0) == '/';
		String[] pArr = StringTool.separateString(path, "/", true);
		int upCount = 0;
		int currentIndex = 0;
		for (int i = 0; i < pArr.length; i++)
		{
			String unit = pArr[i];
			if (unit.length() == 0)
			{
				// 多个分隔符如"//", 作为一个处理
			}
			if ("..".equals(unit))
			{
				// 如果为".."目录需要向前一层
				if (currentIndex > 0)
				{
					currentIndex--;
				}
				else
				{
					upCount++;
				}
			}
			else if (!".".equals(unit))
			{
				// 如果不为"."目录需要向后一层
				pArr[currentIndex++] = unit;
			}
		}
		if (pCount != null)
		{
			pCount.value = absolutePath ? -1 : upCount;
		}
		String[] result;
		if (currentIndex < pArr.length)
		{
			// 如果实际的目录数小于已切分的单元, 则要重新构造数组
			result = new String[currentIndex];
			System.arraycopy(pArr, 0, result, 0, currentIndex);
		}
		else
		{
			result = pArr;
		}
		return result;
	}

	/**
	 * 将一个路径单元合并成一个路径.
	 *
	 * @param rootArr  根路径单元
	 * @param pCount   路径需要向上递归的层数
	 * @param unitArr  需要合并成路径的路径单元
	 * @param isDir    合并后的路径是否为目录
	 * @return  合并后的路径
	 */
	protected static String mergePath(String[] rootArr, int pCount,
			String[] unitArr, boolean isDir)
	{
		if (rootArr == null && unitArr.length == 1)
		{
			// 只有一个unit单元, 不通过StringAppender构造
			String result = "/".concat(unitArr[0]);
			return isDir ? result.concat("/") : result;
		}
		StringAppender buf = StringTool.createStringAppender(32);
		if (pCount >= 0 && rootArr != null && rootArr.length > 0)
		{
			int count = rootArr.length - pCount;
			for (int i = 0; i < count; i++)
			{
				buf.append('/').append(rootArr[i]);
			}
		}
		for (int i = 0; i < unitArr.length; i++)
		{
			buf.append('/').append(unitArr[i]);
		}
		if (isDir)
		{
			buf.append('/');
		}
		return buf.length() > 0 ? buf.toString() : "/";
	}

	/**
	 * 去除路径起始部分的分隔符"/";
	 */
	protected static String trimBeginSplit(String path)
	{
		if (StringTool.isEmpty(path))
		{
			return path;
		}
		int count = path.length();
		int beginIndex = 0;
		for (int i = 0; i < count; i++)
		{
			if (path.charAt(i) != '/')
			{
				beginIndex = i;
				break;
			}
			else
			{
				beginIndex = i + 1;
			}
		}
		return beginIndex == 0 ? path : path.substring(beginIndex);
	}

	public String toString()
	{
		return this.getConfig();
	}

}

/**
 * classpath中的资源.
 */
class ClassPathResource extends AbstractResource
		implements ConfigResource
{
	public ConfigResource create(String config, FactoryContainer container)
	{
		this.container = container;
		ClassLoader loader = (ClassLoader) container.getAttribute(
				FactoryContainer.CLASSLOADER_FLAG);
		int index = config.indexOf(':');
		ClassPathResource res = new ClassPathResource();
		res.prefix = config.substring(0, index + 1);
		IntegerRef pCount = new IntegerRef();
		String tmpPath = mergePath(null, 0, parsePath(config.substring(index + 1), pCount), false);
		if (pCount.value > 0)
		{
			throw new EternaException("Error classpath [" + config + "].");
		}
		res.path = trimBeginSplit(tmpPath);
		if (loader == null)
		{
			loader = this.getClass().getClassLoader();
		}
		res.loader = loader;
		res.url = loader.getResource(res.path);
		res.init(config, container, res.url);
		return res;
	}

	private String prefix;
	private URL url;
	private String path;
	private ClassLoader loader;

	public String getURI()
	{
		if (this.url != null)
		{
			return this.url.toString();
		}
		return null;
	}

	public InputStream getAsStream()
	{
		if (this.getType() != RES_TYPE_FILE)
		{
			return null;
		}
		if (this.resFile != null)
		{
			// 如果存在文件, 直接通过文件读取, 可以不会受JVM缓存影响
			try
			{
				return new FileInputStream(this.resFile);
			}
			catch (IOException ex) {}
		}
		return this.loader.getResourceAsStream(this.path);
	}

	/**
	 * 检查一个地址中是否包含protocol.
	 */
	private static String checkProtocol(String url)
	{
		int index = url.indexOf(':');
		if (index == -1 || index <= 1)
		{
			// 如果字符串中没有protocol或只是盘符, 默认添上file
			url = "file:".concat(url);
		}
		return url;
	}

	public ConfigResource[] listResources(boolean recursive)
	{
		if (this.getType() != RES_TYPE_DIR)
		{
			return null;
		}
		try
		{
			boolean rootPath = this.path.length() == 0;
			// path中最后的"/"会被去掉, 这里需要补上, 但root不需要
			String tmpRoot = rootPath ? "" : this.path.concat("/");
			Enumeration resources = this.loader.getResources(this.path);
			Map result = new HashMap();
			while (resources.hasMoreElements())
			{
				URL res = (URL) resources.nextElement();
				String protocol = res.getProtocol();
				if ("file".equals(protocol))
				{
					File file = new File(res.getFile());
					this.findResources(result, file, recursive, tmpRoot);
				}
				else if ("jar".equals(protocol) || "zip".equals(protocol))
				{
					String tmp = res.toString();
					int index = tmp.indexOf('!');
					tmp = checkProtocol(tmp.substring(4 /* jar: or zip: */, index));
					URL tmpRes = new URL(tmp);
					ZipInputStream stream = new ZipInputStream(tmpRes.openStream());
					this.findResources(result, stream, recursive, tmpRoot);
					stream.close();
				}
				else
				{
					Tool.log.error("Error URL [" + res + "].");
				}
			}
			if (rootPath)
			{
				// 如果是根目录, 只能搜到目录路径, 这里需要将所有的jar搜一边
				// 正常的jar包都会有META-INF目录
				resources = this.loader.getResources("META-INF/");
				while (resources.hasMoreElements())
				{
					URL res = (URL) resources.nextElement();
					String protocol = res.getProtocol();
					if ("jar".equals(protocol) || "zip".equals(protocol))
					{
						String tmp = res.toString();
						int index = tmp.indexOf('!');
						tmp = checkProtocol(tmp.substring(4 /* jar: or zip: */, index));
						URL tmpRes = new URL(tmp);
						ZipInputStream stream = new ZipInputStream(tmpRes.openStream());
						this.findResources(result, stream, recursive, tmpRoot);
						stream.close();
					}
				}
			}
			int count = result.size();
			ConfigResource[] arr = new ConfigResource[count];
			Iterator itr = result.keySet().iterator();
			// tmpRoot中没有起始的"/", 这里需要在前缀补上
			String tmpPrefix = this.prefix.concat("/");
			for (int i = 0; i < count; i++)
			{
				String config = tmpPrefix.concat((String) itr.next());
				arr[i] = this.create(config, this.container);
			}
			return arr;
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
	}
	private void findResources(Map result, ZipInputStream zipStream,
			boolean recursive, String parent)
			throws IOException
	{
		ZipEntry entry = zipStream.getNextEntry();
		while (entry != null)
		{
			String name = entry.getName();
			if (name.length() > parent.length() && name.startsWith(parent))
			{
				// name > parent 不要包含parent目录
				this.dealEntryName(result, parent, name, recursive);
			}
			entry = zipStream.getNextEntry();
		}
	}
	/**
	 * 将zip中的路径信息添加的结果集中.
	 */
	private void dealEntryName(Map result, String parent, String path, boolean recursive)
	{
		String tmp = path.substring(parent.length());
		int index = tmp.indexOf('/');
		if (index == -1)
		{
			result.put(path, Boolean.TRUE);
		}
		else
		{
			if (recursive)
			{
				String tmpPath = parent.concat(tmp.substring(0, index + 1));
				while (!result.containsKey(tmpPath))
				{
					result.put(tmpPath, Boolean.TRUE);
					tmp = tmp.substring(0, index);
					index = tmp.indexOf('/');
					if (index == -1)
					{
						break;
					}
					tmpPath = parent.concat(tmp.substring(0, index + 1));
				}
				result.put(path, Boolean.TRUE);
			}
			else
			{
				result.put(parent.concat(tmp.substring(0, index + 1)), Boolean.TRUE);
			}
		}
	}
	private void findResources(Map result, File path, boolean recursive, String parent)
	{
		File[] arr = path.listFiles();
		for (int i = 0; i < arr.length; i++)
		{
			String name = arr[i].getName();
			if (arr[i].isDirectory())
			{
				String nowConfig = parent.concat(name).concat("/");
				result.put(nowConfig, Boolean.TRUE);
				if (recursive)
				{
					this.findResources(result, arr[i], recursive, nowConfig);
				}
			}
			else
			{
				result.put(parent.concat(name), Boolean.TRUE);
			}
		}
	}

	public ConfigResource getResource(String path)
	{
		ConfigResource res = this.checkFlag(path);
		if (res != null)
		{
			return res;
		}
		IntegerRef pCount = new IntegerRef();
		String[] pArr = parsePath(path, pCount);
		if (pCount.value == -1)
		{
			return this.create(this.prefix.concat(path), this.container);
		}
		int upCount = pCount.value + (this.getType() == RES_TYPE_FILE ? 1 : 0);
		String mPath = mergePath(parsePath(this.path, null), upCount, pArr, path.endsWith("/"));
		return this.create(this.prefix.concat(mPath), this.container);
	}

}

/**
 * 文件系统中的资源.
 */
class FileResource extends AbstractResource
		implements ConfigResource
{
	public ConfigResource create(String config, FactoryContainer container)
	{
		int index = config.indexOf(':');
		try
		{
			File file;
			if (index != -1 && "file".equalsIgnoreCase(config.substring(0, index)))
			{
				URL url = new URL(config);
				file = new File(url.getFile());
			}
			else
			{
				boolean needChange = File.separatorChar != '/';
				if (needChange)
				{
					config = config.replace(File.separatorChar, '/');
				}
				IntegerRef pCount = new IntegerRef();
				String tmpPath = mergePath(null, 0, parsePath(config, pCount), false);
				if (pCount.value != -1 || index != -1)
				{
					// 不是根路径或带有盘符去除起始部分的"/"
					tmpPath = trimBeginSplit(tmpPath);
				}
				if (needChange)
				{
					tmpPath = tmpPath.replace('/', File.separatorChar);
				}
				if (pCount.value != 0)
				{
					// 为根路径或相对父路径, 需要向上获取当前路径的父路径
					File cFile = (new File("")).getAbsoluteFile();
					file = new File(getParent(cFile, pCount.value), tmpPath);
				}
				else
				{
					file = new File(tmpPath);
				}
			}
			file = file.getAbsoluteFile();
			FileResource res = new FileResource();
			res.resFile = file;
			res.init(config, container, null);
			return res;
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
	}
	private ConfigResource create0(String config, File newFile)
	{
		FileResource res = new FileResource();
		res.resFile = newFile;
		res.init(config, this.container, null);
		return res;
	}

	public String getURI()
	{
		if (this.resFile.exists())
		{
			return this.resFile.toString();
		}
		return null;
	}

	public InputStream getAsStream()
	{
		if (this.getType() != RES_TYPE_FILE)
		{
			return null;
		}
		try
		{
			if (this.resFile.isFile())
			{
				return new FileInputStream(this.resFile);
			}
			return null;
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
	}

	public ConfigResource[] listResources(boolean recursive)
	{
		if (this.getType() != RES_TYPE_DIR)
		{
			return null;
		}
		if (this.resFile.isDirectory())
		{
			List result = new ArrayList();
			this.findResources(result, this.resFile, recursive, this.config);
			ConfigResource[] arr = new ConfigResource[result.size()];
			result.toArray(arr);
			return arr;
		}
		return null;
	}
	private void findResources(List result, File path, boolean recursive, String parent)
	{
		File[] arr = path.listFiles();
		for (int i = 0; i < arr.length; i++)
		{
			String name = arr[i].getName();
			if (arr[i].isDirectory())
			{
				String nowConfig = parent.concat(name).concat("/");
				result.add(this.create0(nowConfig, arr[i]));
				if (recursive)
				{
					this.findResources(result, arr[i], recursive, nowConfig);
				}
			}
			else
			{
				result.add(this.create0(parent.concat(name), arr[i]));
			}
		}
	}

	public ConfigResource getResource(String path)
	{
		ConfigResource res = this.checkFlag(path);
		if (res != null)
		{
			return res;
		}
		IntegerRef pCount = new IntegerRef();
		String[] pArr = parsePath(path, pCount);
		File baseFile = this.getType() == RES_TYPE_FILE ?
				this.resFile.getParentFile() : this.resFile;
		File pFile = getParent(baseFile, pCount.value);
		File tFile = this.makeFileByUnit(pFile, pArr);
		if (pCount.value == -1)
		{
			return this.create0(path, tFile);
		}
		int lastPos = this.config.lastIndexOf('/');
		String mPath = lastPos == -1 ? path
				: this.config.substring(0, lastPos + 1).concat(path);
		return this.create0(mPath, tFile);
	}

	private File makeFileByUnit(File rootPath, String[] units)
	{
		String tmpPath = trimBeginSplit(mergePath(null, 0, units, false));
		if (File.separatorChar != '/')
		{
			tmpPath = tmpPath.replace('/', File.separatorChar);
		}
		return new File(rootPath, tmpPath);
	}

	/**
	 * 获取父路径.
	 *
	 * @param pCount  父路径的层数, -1表示获取根路径
	 */
	private static File getParent(File file, int pCount)
	{
		File result = file;
		for (int i = 0; i < pCount || pCount == -1; i++)
		{
			File p = result.getParentFile();
			if (p == null)
			{
				// 已到根路径
				return result;
			}
			result = p;
		}
		return result;
	}

}

/**
 * web中的资源.
 */
class WebResource extends AbstractResource
		implements ConfigResource
{
	public ConfigResource create(String config, FactoryContainer container)
	{
		int index = config.indexOf(':');
		WebResource res = new WebResource();
		res.prefix = config.substring(0, index + 1);
		res.path = config.substring(index + 1);
		if (!res.path.startsWith("/"))
		{
			throw new EternaException("Error path [" + res.path + "] for web.");
		}
		res.context = (javax.servlet.ServletContext) container.getAttribute(
				FactoryContainer.SERVLET_FLAG);
		if (res.context == null)
		{
			String msg = "Not found servlet with key \""
					+ FactoryContainer.SERVLET_FLAG+ "\" in FactoryContainer ["
					+ container.getId() + "].";
			throw new EternaException(msg);
		}
		try
		{
			res.url = res.context.getResource(res.path);
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
		res.init(config, container, res.url);
		return res;
	}

	private String prefix;
	private URL url;
	private String path;
	private javax.servlet.ServletContext context;

	public String getURI()
	{
		if (this.url != null)
		{
			return this.url.toString();
		}
		return null;
	}

	public InputStream getAsStream()
	{
		if (this.getType() != RES_TYPE_FILE)
		{
			return null;
		}
		if (this.resFile != null)
		{
			try
			{
				return new FileInputStream(this.resFile);
			}
			catch (IOException ex) {}
		}
		return this.context.getResourceAsStream(this.path);
	}

	public ConfigResource[] listResources(boolean recursive)
	{
		if (this.getType() != RES_TYPE_DIR)
		{
			return null;
		}
		Set paths = this.context.getResourcePaths(this.path);
		List result = new ArrayList();
		this.findResources(result, paths, recursive, this.prefix);
		ConfigResource[] arr = new ConfigResource[result.size()];
		result.toArray(arr);
		return arr;
	}
	private void findResources(List result, Set paths, boolean recursive, String prefix)
	{
		Iterator itr = paths.iterator();
		while (itr.hasNext())
		{
			String name = (String) itr.next();
			String nowConfig = prefix.concat(name);
			result.add(this.create(nowConfig, this.container));
			if (recursive && name.endsWith("/"))
			{
				Set tmp = this.context.getResourcePaths(name);
				this.findResources(result, tmp, recursive, nowConfig);
			}
		}
	}

	public ConfigResource getResource(String path)
	{
		ConfigResource res = this.checkFlag(path);
		if (res != null)
		{
			return res;
		}
		IntegerRef pCount = new IntegerRef();
		String[] pArr = parsePath(path, pCount);
		if (pCount.value == -1)
		{
			return this.create(this.prefix.concat(path), this.container);
		}
		int upCount = pCount.value + (this.getType() == RES_TYPE_FILE ? 1 : 0);
		String mPath = mergePath(parsePath(this.path, null), upCount, pArr, path.endsWith("/"));
		return this.create(this.prefix.concat(mPath), this.container);
	}

}

/**
 * url中的资源.
 */
class UrlResource extends AbstractResource
		implements ConfigResource
{
	public ConfigResource create(String config, FactoryContainer container)
	{
		UrlResource res = new UrlResource();
		try
		{
			res.url = new URL(config);
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
		res.init(config, container, res.url);
		return res;
	}

	private URL url;

	public String getURI()
	{
		if (this.url != null)
		{
			return this.url.toString();
		}
		return null;
	}

	public InputStream getAsStream()
	{
		if (this.getType() != RES_TYPE_FILE)
		{
			return null;
		}
		try
		{
			return this.url.openStream();
		}
		catch (IOException ex)
		{
			return null;
		}
	}

	public ConfigResource[] listResources(boolean recursive)
	{
		// url模式无法列出子目录
		return null;
	}

	public ConfigResource getResource(String path)
	{
		ConfigResource res = this.checkFlag(path);
		if (res != null)
		{
			return res;
		}
		try
		{
			URL tmp = new URL(this.url, path);
			return this.create(tmp.toString(), this.container);
		}
		catch (IOException ex)
		{
			return null;
		}
	}

}
