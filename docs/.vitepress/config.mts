import { defineConfig } from 'vitepress'

export default defineConfig({
  base: '/mcn/',
  lang: 'zh-CN',
  title: 'Mcn文档',
  description: '一个基于SpringBoot&Cloud的快速开发工具包',
  head: [['link', { rel: 'icon', href: '/favicon.ico' }]],
  lastUpdated: true,
  themeConfig: {
    logo: '/images/logo.png',
    editLink: {
      pattern: 'https://github.com/kse-music/mcn-boot-project/edit/master/docs/:path',
      text: '在 GitHub 上编辑此页',
    },
    lastUpdated: {
      text: '上次更新',
    },
    nav: [
      { text: '源码', link: 'https://github.com/kse-music/mcn-boot-project' },
      { text: '更新日志', link: '/changelog' },
      {
        text: '模板',
        items: [
          { text: 'Meta-Boot', link: 'https://github.com/kse-music/meta-boot' },
          { text: 'Meta-Script', link: 'https://github.com/kse-music/meta-script' },
        ],
      },
      { text: '社区', link: 'http://www.hiboot.cn' },
    ],
    sidebar: [
      {
        text: '前言',
        collapsed: false,
        items: [
          { text: '简介', link: '/intro/preface' },
          { text: '环境准备', link: '/intro/ready' },
        ],
      },
      {
        text: '使用教程',
        collapsed: false,
        items: [
          { text: '快速开始', link: '/guide/quick-start' },
          { text: '依赖管理', link: '/guide/dependency' },
        ],
      },
      {
        text: '功能说明',
        collapsed: false,
        items: [
          { text: '通用功能', link: '/function/common' },
          { text: '扩展功能', link: '/function/extension' },
          { text: '自动配置', link: '/function/autoconfig' },
        ],
      },
      {
        text: 'SpringBoot',
        collapsed: false,
        items: [
          { text: '核心组件', link: '/spring/core' },
          { text: '生命周期', link: '/spring/lifecycle' },
          { text: '源码解析', link: '/spring/source-parse' },
        ],
      },
      {
        text: '参考',
        collapsed: false,
        items: [{ text: '参考资料', link: '/refer' }],
      },
    ],
    socialLinks: [{ icon: 'github', link: 'https://github.com/kse-music/mcn-boot-project' }],
    search: {
      provider: 'local',
    },
    footer: {
      message: 'MIT Licensed',
      copyright: 'Copyright © 2023 HiBoot',
    },
  },
})
