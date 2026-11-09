# JSFuck for KubeJS

1.在 [JSFuck 编码器](https://jsfuck.com/) 输入 KubeJS 代码，例如 `console.info("JSFuck loaded")`。勾选 **Eval Source**。
2.将编码结果保存为 `kubejs/startup_scripts/hello.js`，重启后在 KubeJS 启动日志里查看输出。服务端事件脚本和客户端脚本照常放在对应的 `server_scripts` 和 `client_scripts` 目录。

支持官网编码器和历史版的 `at`、`flat`、`fill`、`filter` 包装。启用 **Run In Parent Scope** 时，请把完整的六字符输出保存为单独的脚本，解码出的顶层声明进入当前 KubeJS 的全局作用域。原始脚本使用 KubeJS 的 API 和 Rhino 支持的 JavaScript 语法。

