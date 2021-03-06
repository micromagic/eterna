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

package self.micromagic.eterna.share;


/**
 * 获取对象的工厂.
 */
public interface Factory
{
	/**
	 * 工厂中注册的对象的最大数目.
	 */
	static final int MAX_OBJECT_COUNT = 1024 * 32;

	/**
	 * 对工厂进行初始化.
	 *
	 * @param factoryContainer  工厂所属的容器
	 * @param shareFactory      共享对象的工厂
	 * @return  是否已初始化
	 *          false表示是第一次初始化, true表示已执行过初始化
	 */
	boolean initialize(FactoryContainer factoryContainer, Factory shareFactory)
			throws EternaException;

	/**
	 * 判断当前工厂是否有效. <p>
	 * 已销毁的工厂为无效工厂.
	 */
	boolean isValid() throws EternaException;

	/**
	 * 设置工厂的名称.
	 */
	void setName(String name) throws EternaException;

	/**
	 * 获取工厂的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获取工厂所属的容器.
	 */
	FactoryContainer getFactoryContainer() throws EternaException;

	/**
	 * 注册一个对象到工厂中.
	 * 所注册的对象需要实现特定的方法能够获取名称.
	 */
	void registerObject(Object obj) throws EternaException;

	/**
	 * 注册一个对象到工厂中.
	 *
	 * @param name  对象注册的名称
	 */
	void registerObject(String name, Object obj) throws EternaException;

	/**
	 * 从工厂中创建一个对象.
	 */
	Object createObject(Object key) throws EternaException;

	/**
	 * 根据给出的编号创建对象.
	 */
	Object createObject(int id) throws EternaException;

	/**
	 * 查询已注册的对象的编号.
	 */
	int findObjectId(Object key) throws EternaException;

	/**
	 * 获取某种类型的对象的所有名称.
	 */
	String[] getObjectNames(Class type) throws EternaException;

	/**
	 * 获取工厂中注册的对象个数.
	 */
	int getObjectCount() throws EternaException;

	/**
	 * 判断某个对象是否存在.
	 *
	 * @param key         对象的名称
	 * @param checkOther  是否需要在其他工厂中检查对象
	 */
	boolean isObjectExists(Object key, boolean checkOther) throws EternaException;

	/**
	 * 判断某个对象是否为单例.
	 */
	boolean isSingleton(Object key) throws EternaException;

	/**
	 * 根据名称获取一个属性.
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 判断工厂中是否拥有某个属性.
	 * 只判断本工厂的, 不包括共享工厂中的属性.
	 */
	boolean hasAttribute(String name) throws EternaException;

	/**
	 * 获取工厂中的属性名称列表.
	 * 如果没有任何属性则返回一个空的数组.
	 */
	String[] getAttributeNames() throws EternaException;

	/**
	 * 设置一个属性.
	 */
	Object setAttribute(String name, Object value) throws EternaException;

	/**
	 * 移除一个属性.
	 */
	Object removeAttribute(String name) throws EternaException;

	/**
	 * 销毁工厂及工厂中的对象.
	 */
	void destroy();

}
