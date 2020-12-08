<img src="https://img.shields.io/badge/springboot-2.2.7.RELEASE-brightgreen" alt="springboot"/>  <img src="https://img.shields.io/badge/jdk-1.8-blue" alt="java"/>  <img src="https://img.shields.io/badge/minio-7.1.4-green" alt="minio"/>   <img src="https://img.shields.io/badge/elasticsearch-6.7.2-yellowgreen" alt="elasticsearch"/>

# 个人图床(持续开发中。。。)
使用springboot+minio+elasticsearch+webuploader实现图床，支持给图片打标签进行搜索，支持图片压缩，支持分片上传，秒传，断点续传。


## 功能

- [x] 对象存储
- [x] 标签高亮搜索
- [x] 图片压缩
- [x] 秒传
- [ ] 分片上传
- [ ] 断点续传
- [ ] docker化
- [ ] 移动端（微信小程序）

## 技术

### 前端

- 前端样式基于`materialize.css`框架实现
- 前端上传插件基于`webuploader`改造，改造参考我的另外一个仓库：[webuploader](https://github.com/tuituidan/webuploader)

### 后台

- 采用`minio`进行对象存储
- 使用`elasticsearch`对图片标签进行搜索
- `jpg`的压缩使用开源组件[thumbnailator](https://github.com/coobird/thumbnailator)，`png`压缩使用`OpenViewerFX`中的`PngCompressor`
- 上传接口除了`webuploader`使用的，还提供了一个`base64`图片上传接口

> elasticsearch需要安装ik中文分词插件


## 图片上传页

![index](https://camo.githubusercontent.com/8cd8dc22888ab46d0112330556744e9ae4e989f4/68747470733a2f2f63646e2e737461746963616c792e636f6d2f67682f74756974756964616e2f696d6167652d686f73742f646576656c6f702f646f63732f73686f772f696e6465782e6a7067)

## 图片搜索页

![index](https://camo.githubusercontent.com/233a1874584c77bb867f43ab3ff06a61f03ebbb3/68747470733a2f2f63646e2e737461746963616c792e636f6d2f67682f74756974756964616e2f696d6167652d686f73742f646576656c6f702f646f63732f73686f772f7365617263682e6a7067)
