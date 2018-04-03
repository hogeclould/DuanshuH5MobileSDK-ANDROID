/*
 *author zhangzhen
 * date 2015-4-21
 * version 1.0
 * */
(function() {
    var Duanshu = function() {
        var _version = "beta",
        	_debug = false,
        	_bridgeInit = false,
        	_registerevents = null;
        var responseCallbacks = {};
        var uniqueId = 1;
        /*API方法集合*/
        var _apiArray = [
            'getUserInfo',
            'previewImage',
            'previewPic',
            'startRecord',
            'stopRecord',
            'playVoice',
            'pauseVoice',
            'stopVoice',
            'voiceTimeObserver',
            'loadUrl',
            'chooseImage',
            'share'
        ];
        var _privateMethod = {
            getPlatform: function() {
                var platform = navigator.userAgent.toLowerCase();
                if (/iphone|ipod|ipad/gi.test(platform)) {
                    return "iOS";
                } else if (/android/gi.test(platform)) {
                    return "Android";
                } else {
                    return "不支持此平台!";
                }
            },
 
            setupWebViewJavascriptBridge:function(callback) {
                if (window.WebViewJavascriptBridge) { return callback(WebViewJavascriptBridge); }
                if (window.WVJBCallbacks) { return window.WVJBCallbacks.push(callback); }
                window.WVJBCallbacks = [callback];
                var WVJBIframe = document.createElement('iframe');
                WVJBIframe.style.display = 'none';
                WVJBIframe.src = 'https://__bridge_loaded__';
                document.documentElement.appendChild(WVJBIframe);
                setTimeout(function() { document.documentElement.removeChild(WVJBIframe) }, 0)
            },
            
            connectWebViewJavascriptBridge: function(callback) {
                if (window.WebViewJavascriptBridge) {
                    callback(WebViewJavascriptBridge);
                } else {
					document.addEventListener('WebViewJavascriptBridgeReady', function() {
						callback(WebViewJavascriptBridge);
					}, false);
                   this.setupWebViewJavascriptBridge(callback);
                }
                setTimeout(function(){
                	if (!window.WebViewJavascriptBridge) {
                		alert('客户端版本过低,请升级客户端');
                	}
                },3000);
            },
            callApiCenter: function(apikey, options) {
                var platform = this.getPlatform(), 
                	apikey = apikey, 
                	params = ( options && options.param ) ? options.param : null,
                	callback = ( options && options.callback ) ? options.callback : null;
                if (platform == "iOS") {
                    this.connectWebViewJavascriptBridge(function(bridge) {              
                        if (!_bridgeInit) {  
                            if (bridge.init) {
                                bridge.init(function(message, responseCallback) {
                                    var message = JSON.parse( message );
                                    var eventName = message.eventName, data = message.data;
                                    if( _registerevents && ( typeof _registerevents[eventName] == 'function' )){
                                        _registerevents[eventName]( data );
                                    }
                                });
                                _bridgeInit = true;
                            }              
                        }
                        
                        bridge.callHandler('webviewBridge', {
                        	api : {
                        		method : apikey,
                        		debug : _debug
                        	},
                        	params : params
                        }, function(response) {
                        	_debug && alert( response );
                            typeof callback == "function" && callback(response);
                            return response;
                        });
                    });
                } 
                else if(platform == "Android"){
                    if(typeof callback == "function"){
                        var callbackId = 'cb_' + (uniqueId++) + '_' + new Date().getTime();
                        responseCallbacks[callbackId] = callback;
                        webviewBridge.dispatchMethod(JSON.stringify({api:{method:apikey,debug:_debug},params:params,callBackId:callbackId}));
                    }else{
                        webviewBridge.dispatchMethod(JSON.stringify({api:{method:apikey,debug:_debug},params:params}));
                    }

                } else {
                    alert(platform);
                }
            }
        };
        return function() {
        	this.config = function(options,callback){
        		_debug = options.debug || false;
        		return this.callApiCenter("checkJsApi", {callback:callback,param : options});
        	};
            this.getVersion = function() {
                return _version;
            };
            this.getApiCollect = function(){
            	return _apiArray;
            };
            this.registerEvents = function(options){
				_registerevents = options;
			};
			this.getEvents = function(){
            				return _registerevents;
            			};
            this.dispatchMessageFromNativeByCallbackId = function(messageJSON){
                            var message = JSON.parse(messageJSON);
                            var responseCallback;
                            if (message.responseId) {
                                responseCallback = responseCallbacks[message.responseId];
                                if (!responseCallback) {
                                    return;
                                }
                                responseCallback(message.responseData);
                                delete responseCallbacks[message.responseId];
                            }
                        };
            this.callApiCenter = function(apikey, options) {
                _privateMethod.callApiCenter(apikey, options);
                 return this;
            };
            this.registerApi();
        };
    }();
    Duanshu.prototype = {
			registerApi : function(){
				var _this = this,
					apiArray = this.getApiCollect();
				for(var i = 0; i<apiArray.length; i++){
					var apikey = apiArray[i];
					Duanshu.prototype[apikey] = (function(apikey){
						return function(){
							var len = arguments.length;
							if(!len){
								_this.callApiCenter(apikey);
							}
							if(len){
								var arr0 = arguments[0],
									arr1 = arguments[1];
								if( typeof arr0 == "function" ){
									_this.callApiCenter(apikey,{callback:arr0});
								}else{
									if( arr1 && ( typeof arr1 == "function" ) ){
										_this.callApiCenter(apikey,{param :arr0, callback:arr1 });
									}else{
										_this.callApiCenter(apikey,{param :arr0 });
									}
								}
							}
						};
					})(apikey);
							
				}
			}
    };


    window.duanshu = new Duanshu();
})();
