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

package self.micromagic.util.logging;

/**
 * 日志的监听器.
 */
public interface LoggerListener
{
	/**
	 * 当日志记录之后将触发此方法.
	 *
	 * @param msg         日志的文本信息
	 * @param ex          异常信息
	 * @param level       日志的等级
	 * @param threadName  日志所在的线程
	 * @param className   日志所在的类
	 * @param methodName  日志所在的方法
	 * @param fileName    日志所在的文件
	 * @param lineNumber  日志所在的行
	 */
	void afterLog(String msg, Throwable ex, String level, String threadName, String className,
			String methodName, String fileName, String lineNumber);

}
