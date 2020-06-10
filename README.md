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

# Future

- 多线程多Peer下载
