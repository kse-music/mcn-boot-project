module.exports = {
    base: '/',
    port: 80,
    lang: 'zh-CN',
    title: 'Mcn文档',
    temp:'/tmp/.temp',
    description: '一个基于springboot 2.7+版本的快速开发工具包',
    plugins: [
        '@vuepress/back-to-top'
    ],
    themeConfig: {
        editLinks : true,
        editLinkText: "在 GitHub 上编辑此页",
        lastUpdated: "上次更新",
        algolia: {},
        nav: [
            {text : '源码',link : 'https://github.com/kse-music/mcn-boot-project'},
            {text : '更新日志',link : '/changelog', ariaLabel: '更新日志'},
            {
                text : '模板',
                items : [{
                    text : 'Meta-Boot',
                    link : 'https://github.com/kse-music/meta-boot'
                },{
                    text : 'Meta-Script',
                    link : 'https://github.com/kse-music/meta-script'
                }]
            },
            {text: '社区', link: 'http://www.hiboot.cn'}
        ],
        sidebar:[{
            title : '前言',
            collapsable: false,
            children: ['intro/preface','intro/ready']
        },{
            title : '使用教程',
            collapsable: false,
            children: ['guide/quick-start']
        },{
            title: '功能说明',
            collapsable: false,
            children: ['function/common','function/extension','function/autoconfig']
        },{
            title: '参考',
            collapsable: false,
            children: ['refer']
        }]
    }
}