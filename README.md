# netty-bt

基于netty的bt下载器.

# 设计模式

## Builder

- [TCPClient](./src/main/java/xyz/zzyitj/nbt/client/TCPClient.java)
- [UTPClient](./src/main/java/xyz/zzyitj/nbt/client/UTPClient.java)
- [TCPServer](./src/main/java/xyz/zzyitj/nbt/server/TCPServer.java)
- [UTPServer](./src/main/java/xyz/zzyitj/nbt/server/UTPServer.java)
- [TCPClientHandler](./src/main/java/xyz/zzyitj/nbt/handler/TCPClientHandler.java)
- [TCPServerHandler](./src/main/java/xyz/zzyitj/nbt/handler/TCPServerHandler.java)

# 功能

- 单Peer单文件单区块下载
- 单Peer单文件多区块下载
- 单Peer多文件单区块下载
- 单Peer多文件多区块下载
- 多Peer多文件多区块下载

# Future

- 指定文件下载
- 断点续传
- 做种功能
- UTP协议的实现

# License

    MIT License

    Copyright (c) 2020 GuaSeed

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
