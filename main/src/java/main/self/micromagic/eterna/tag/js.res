# Copyright 2015 xinjunli (micromagic@sina.com).
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# @author micromagic@sina.com
#
# 标签中需要输出的js脚本
#
# init标签使用的js脚本
## init.js
var retryFind = false;

var eInitFn = function ()
{
	_eterna = new Eterna(eternaData, eterna_debug, null);
	_eterna.cache.useAJAX = needAJAX;
	if (retryFind)
	{
		_eterna.cache.retryFindCount = 5;
	}
	_eterna.reInit();
}

var eCheckInitFn = function ()
{
	if (typeof jQuery != "undefined" && typeof Eterna != "undefined")
	{
		eInitFn();
	}
	else
	{
		setTimeout(eCheckInitFn, 50);
	}
}

if (typeof jQuery != "undefined")
{
	jQuery(eCheckInitFn);
}
else
{
	retryFind = true;
	setTimeout(eCheckInitFn, 50);
}

# res标签使用的js脚本
## res.js
if (typeof eg_pageInitializedURL == "undefined")
{
	window.eg_pageInitializedURL = {};
	window.ef_loadResource = function (jsResource, url, charset, callback)
	{
		if (window.eg_pageInitializedURL[url])
		{
			if (callback)
			{
				if (window.eg_pageInitializedURL[url].loaded)
				{
					callback();
				}
				else
				{
					setTimeout(function() {ef_loadResource(jsResource, url, charset, callback);}, 50);
				}
			}
			return;
		}
		window.eg_pageInitializedURL[url] = {loaded:0};
		if (typeof eg_resVersion != "undefined")
		{
			if (url.indexOf("?") == -1)
			{
				url += "?_v=" + eg_resVersion;
			}
			else
			{
				url += "&_v=" + eg_resVersion;
			}
		}
		var resObj;
		if (jsResource)
		{
			resObj = document.createElement("script");
			resObj.type = "text/javascript";
			resObj.async = true;
			resObj.src = url;
			resObj.onload = resObj.onreadystatechange = function()
			{
				if (!this.readyState || this.readyState === "loaded" || this.readyState == "complete")
				{
					window.eg_pageInitializedURL[url].loaded = 1;
					if (callback)
					{
						callback();
					}
					resObj.onload = resObj.onreadystatechange = null;
				}
			};
			resObj.onerror = function()
			{
				window.eg_pageInitializedURL[url].loaded = -1;
				if (callback)
				{
					callback();
				}
				resObj.onerror = null;
			};
		}
		else
		{
			resObj = document.createElement("link");
			resObj.type = "text/css";
			resObj.rel = "stylesheet";
			resObj.href = url;
			window.eg_pageInitializedURL[url].loaded = 1;
		}
		if (charset != null)
		{
			resObj.charset = charset;
		}
		var s = document.getElementsByTagName('script')[0];
		s.parentNode.insertBefore(resObj, s);
	}
}
