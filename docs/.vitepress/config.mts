import { defineConfig } from 'vitepress'

const base = '/mcn/'

export default defineConfig({
  base,
  lang: 'zh-CN',
  title: 'MCN Boot',
  description: '基于 Spring Boot 与 Spring Cloud 的 Java 开发工具包。把通用配置留给工具，把创造留给开发者。',
  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: `${base}images/mcn-mark.svg` }],
    ['meta', { name: 'theme-color', content: '#f7f5ef' }],
  ],
  appearance: true,
  lastUpdated: true,
  markdown: { codeCopyButtonTitle: '复制代码' },
  themeConfig: {
    logo: { src: '/images/mcn-mark.svg', alt: '' },
    siteTitle: 'MCN <span class="mcn-brand-weight">Boot</span>',
    outline: { level: [2, 3], label: '本页内容' },
    docFooter: { prev: '上一篇', next: '下一篇' },
    sidebarMenuLabel: '文档导航',
    returnToTopLabel: '回到顶部',
    darkModeSwitchLabel: '阅读外观',
    lightModeSwitchTitle: '切换到浅色模式',
    darkModeSwitchTitle: '切换到深色模式',
    notFound: {
      title: '这一页，还没有找到。',
      quote: '回到清晰的起点，继续构建你的下一个好想法。',
      linkLabel: '返回 MCN Boot 首页',
      linkText: '回到产品首页',
    },
    editLink: {
      pattern: 'https://github.com/kse-music/mcn-boot-project/edit/master/docs/:path',
      text: '在 GitHub 上编辑此页',
    },
    lastUpdated: {
      text: '上次更新',
    },
    nav: [
      { text: '概览', link: '/' },
      { text: '开发文档', link: '/intro/preface', activeMatch: '/(intro|guide|function|spring)/|/refer' },
      { text: '组件生态', link: '/#ecosystem' },
      {
        text: '更多',
        items: [
          { text: '更新日志', link: '/changelog' },
          { text: 'Meta-Boot', link: 'https://github.com/kse-music/meta-boot' },
          { text: 'Meta-Script', link: 'https://github.com/kse-music/meta-script' },
          { text: 'HiBoot 社区', link: 'http://www.hiboot.cn' },
        ],
      },
    ],
    sidebar: [
      {
        text: '认识 MCN Boot',
        collapsed: false,
        items: [
          { text: '简介', link: '/intro/preface' },
          { text: '环境准备', link: '/intro/ready' },
        ],
      },
      {
        text: '开始构建',
        collapsed: false,
        items: [
          { text: '快速开始', link: '/guide/quick-start' },
          { text: '依赖管理', link: '/guide/dependency' },
        ],
      },
      {
        text: '核心能力',
        collapsed: false,
        items: [
          { text: '通用功能', link: '/function/common' },
          { text: '扩展功能', link: '/function/extension' },
          { text: '自动配置', link: '/function/autoconfig' },
        ],
      },
      {
        text: '理解 Spring Boot',
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
      options: {
        translations: {
          button: { buttonText: '搜索文档', buttonAriaLabel: '搜索文档' },
          modal: {
            displayDetails: '显示详细内容',
            resetButtonTitle: '清空搜索',
            backButtonTitle: '返回',
            noResultsText: '没有找到相关内容',
            footer: {
              selectText: '选择',
              selectKeyAriaLabel: '回车选择',
              navigateText: '切换',
              navigateUpKeyAriaLabel: '上一项',
              navigateDownKeyAriaLabel: '下一项',
              closeText: '关闭',
              closeKeyAriaLabel: '按 Esc 关闭',
            },
          },
        },
      },
    },
  },
})
