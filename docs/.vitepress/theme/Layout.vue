<script setup lang="ts">
import { computed } from 'vue'
import { useData, withBase } from 'vitepress'
import DefaultTheme from 'vitepress/theme-without-fonts'
import HomePage from './HomePage.vue'
import SiteIcon from './SiteIcon.vue'

const { page } = useData()
const sections = {
  intro: { title: '认识 MCN Boot', kicker: 'A FAMILIAR FOUNDATION' },
  guide: { title: '开始构建', kicker: 'GET UP AND BUILD' },
  function: { title: '核心能力', kicker: 'THOUGHTFULLY COMPOSED' },
  spring: { title: '理解 Spring Boot', kicker: 'UNDERSTAND THE FOUNDATION' },
}
const section = computed(() => sections[page.value.relativePath.split('/')[0] as keyof typeof sections]
  ?? { title: page.value.title, kicker: 'THE DEVELOPER JOURNAL' })
</script>

<template>
  <DefaultTheme.Layout>
    <template #home-hero-before><HomePage /></template>
    <template #sidebar-nav-before>
      <div class="mcn-sidebar-heading"><SiteIcon name="book" /><span>开发文档</span><small>GUIDE</small></div>
    </template>
    <template #doc-before>
      <div class="mcn-doc-intro">
        <div class="mcn-breadcrumb"><a :href="withBase('/')">MCN Boot</a><span>/</span><span>{{ section.title }}</span></div>
        <p class="mcn-eyebrow">{{ section.kicker }}</p>
      </div>
    </template>
    <template #aside-outline-after>
      <div class="mcn-reading-note">
        <img :src="withBase('/images/mcn-mark.svg')" width="28" height="28" alt="" />
        <strong>从这里，开始构建。</strong>
        <p>熟悉的 Spring。<br />更从容的开发。</p>
        <a :href="withBase('/')">回到产品概览 <SiteIcon name="arrow" /></a>
      </div>
    </template>
    <template #layout-bottom>
      <footer class="mcn-footer">
        <div class="mcn-wrap mcn-footer-inner">
          <a class="mcn-footer-brand" :href="withBase('/')">MCN Boot<span>把基础打好，让业务向前。</span></a>
          <div class="mcn-footer-links">
            <a :href="withBase('/intro/preface.html')">开发文档</a>
            <a href="https://github.com/kse-music/mcn-boot-project" target="_blank" rel="noopener noreferrer">GitHub <SiteIcon name="external" /></a>
            <span>HiBoot · 开源 Java 工具包</span>
          </div>
        </div>
      </footer>
    </template>
  </DefaultTheme.Layout>
</template>
