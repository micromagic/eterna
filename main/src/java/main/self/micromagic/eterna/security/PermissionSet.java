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

package self.micromagic.eterna.security;

/**
 * 权限集合.
 */
public interface PermissionSet
{
	/**
	 * 检测参数permission中是否包含本权限集合中的某个权限.
	 *
	 * 注:
	 * 如果参数为permission空, 则返回true.
	 * 如果本权限集合为空, 则也返回true.
	 */
	boolean checkPermission(Permission permission);

	/**
	 * 返回此权限集合的配置字符串.
	 */
	String toString();

}