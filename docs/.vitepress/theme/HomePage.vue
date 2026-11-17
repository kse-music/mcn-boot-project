<script setup lang="ts">
import { ref } from 'vue'
import { withBase } from 'vitepress'
import { version } from '../../../package.json'
import SiteIcon from './SiteIcon.vue'

const parentCode = `<parent>
    <groupId>cn.hiboot.mcn</groupId>
    <artifactId>mcn-boot-starter-parent</artifactId>
    <version>${version}</version>
</parent>`
const codeParts = parentCode.split(/(<[^>]*>)/g)
const copyLabel = ref('复制')

async function copyCode() {
  try {
    await navigator.clipboard.writeText(parentCode)
    copyLabel.value = '已复制'
  } catch {
    copyLabel.value = '请选择代码手动复制'
  }
}

const capabilities = [
  { number: '01', icon: 'sliders' as const, title: '约定，开箱即用。', text: '统一响应、异常处理、参数校验与跨域配置，让每一个项目都有熟悉的起点。', detail: 'JSON / Validation / Web', link: '/function/autoconfig.html' },
  { number: '02', icon: 'layers' as const, title: '组件，按需组合。', text: '从数据库、缓存到对象存储，只引入所需的依赖，再让条件装配接好余下的部分。', detail: 'JPA / MyBatis / Redis / MinIO', link: '/guide/dependency.html' },
  { number: '03', icon: 'code' as const, title: '生态，保持熟悉。', text: '沿用 Spring Boot 与 Spring Cloud 的开发方式，支持 Servlet、Reactive 和非 Web 应用。', detail: 'Spring Boot / Spring Cloud', link: '/intro/preface.html' },
]
const ecosystems = [
  { number: '01', title: 'Web 与接口', text: 'Servlet / WebFlux / OpenAPI', detail: '响应、异常与接口文档，形成一致的应用边界。', link: '/function/autoconfig.html' },
  { number: '02', title: '数据与存储', text: 'JDBC / JPA / MyBatis / MongoDB', detail: '选择熟悉的数据访问方式，按需要组织存储能力。', link: '/function/autoconfig.html' },
  { number: '03', title: '云与协作', text: 'Redis / MinIO / OpenFeign', detail: '从缓存、文件到服务调用，连接应用的日常所需。', link: '/guide/dependency.html' },
]
</script>

<template>
  <div class="mcn-home">
    <section class="mcn-hero mcn-wrap" aria-labelledby="mcn-hero-title">
      <div class="mcn-issue"><span>MCN BOOT / 开发者的基础工具集</span><span>JAVA · SPRING · OPEN SOURCE</span></div>
      <div class="mcn-hero-grid">
        <div class="mcn-hero-copy">
          <p class="mcn-eyebrow">GOOD FOUNDATIONS. GREAT POSSIBILITIES.</p>
          <h1 id="mcn-hero-title">好工程，<br />从<em>少一点重复</em><br />开始。</h1>
          <p class="mcn-hero-description">把通用配置留给工具，把创造留给开发者。<br />用熟悉的 Spring 生态，构建有条理的 Java 应用。</p>
          <div class="mcn-hero-actions">
            <a class="mcn-button" :href="withBase('/guide/quick-start.html')">翻开开发指南 <SiteIcon name="arrow" /></a>
            <a class="mcn-text-link" href="#ecosystem">看看它如何工作 <SiteIcon name="arrow" /></a>
          </div>
          <p class="mcn-version">MCN Boot {{ version }} <span>—</span> 为每一次认真构建。</p>
        </div>
        <div class="mcn-diagram" role="img" aria-label="应用结构示意：你的业务应用建立在 MCN Boot、Spring 生态与 Java 17 及以上版本的基础上。">
          <div class="mcn-diagram-topline"><span>THE ANATOMY OF A GOOD START</span><span>图 / 01</span></div>
          <div class="mcn-stack">
            <div class="mcn-layer mcn-layer-app"><span>01</span><strong>你的下一个好想法</strong><small>Application / Business</small></div>
            <div class="mcn-layer-join"><span></span><b>留出专注业务的空间</b><span></span></div>
            <div class="mcn-layer mcn-layer-boot"><span>02</span><strong>MCN Boot</strong><small>约定 · 装配 · 扩展</small></div>
            <div class="mcn-layer mcn-layer-spring"><span>03</span><strong>Spring 生态</strong><small>Boot / Cloud / Data</small></div>
            <div class="mcn-layer mcn-layer-java"><span>04</span><strong>Java 17+</strong><small>A familiar foundation.</small></div>
          </div>
          <div class="mcn-diagram-caption"><span>依赖可以很轻。<br />基础要足够扎实。</span><span aria-hidden="true">↗</span></div>
        </div>
      </div>
    </section>

    <section class="mcn-start mcn-wrap" aria-labelledby="mcn-start-title">
      <div>
        <p class="mcn-eyebrow">01 / START WITH THE ESSENTIALS</p>
        <h2 id="mcn-start-title">一份依赖。<br />一个更好的起点。</h2>
        <p class="mcn-section-description">把配置、组件与规范放回它们该在的位置。<br />从清晰的约定开始，沿着自己的业务生长。</p>
        <a class="mcn-text-link" :href="withBase('/guide/quick-start.html')">阅读快速开始 <SiteIcon name="arrow" /></a>
      </div>
      <div class="mcn-code-example">
        <div class="mcn-code-header"><span><SiteIcon name="code" /> pom.xml</span><button type="button" aria-label="复制 Maven 配置" @click="copyCode">{{ copyLabel }}</button></div>
        <pre><code><span v-for="(part, index) in codeParts" :key="index" :class="{ 'mcn-syntax-tag': part.startsWith('<') }">{{ part }}</span></code></pre>
        <div class="mcn-code-caption"><span>已有父 POM？</span><a :href="withBase('/guide/dependency.html#导入-bom')">使用 BOM 接入 <SiteIcon name="arrow" /></a></div>
        <span class="mcn-sr-only" role="status">{{ copyLabel === '复制' ? '' : copyLabel }}</span>
      </div>
    </section>

    <section class="mcn-features mcn-wrap" aria-labelledby="mcn-features-title">
      <div class="mcn-section-heading">
        <div><p class="mcn-eyebrow">02 / THOUGHTFULLY COMPOSED</p><h2 id="mcn-features-title">好的工具，<br /><em>让工作更从容。</em></h2></div>
        <p>不改变你熟悉的开发方式，<br />在重复之处，往前一步。</p>
      </div>
      <div class="mcn-capabilities">
        <article v-for="item in capabilities" :key="item.number">
          <div class="mcn-cap-top"><span>{{ item.number }}</span><SiteIcon :name="item.icon" /></div>
          <h3><a :href="withBase(item.link)">{{ item.title }}</a></h3><p>{{ item.text }}</p>
          <div class="mcn-cap-bottom"><span>{{ item.detail }}</span><a :href="withBase(item.link)" :aria-label="`了解${item.title}`"><SiteIcon name="arrow" /></a></div>
        </article>
      </div>
    </section>

    <section id="ecosystem" class="mcn-ecosystem mcn-wrap" aria-labelledby="mcn-ecosystem-title">
      <div class="mcn-section-heading">
        <div><p class="mcn-eyebrow">03 / COMPOSABLE BY NATURE</p><h2 id="mcn-ecosystem-title">基础设施，各就其位。</h2></div>
        <p>保留熟悉的技术选择。<br />让通用能力在应用之下自然衔接。</p>
      </div>
      <div class="mcn-ecosystem-list">
        <a v-for="item in ecosystems" :key="item.number" :href="withBase(item.link)" class="mcn-ecosystem-row">
          <span class="mcn-row-number">{{ item.number }}</span><h3>{{ item.title }}</h3><div><strong>{{ item.text }}</strong><p>{{ item.detail }}</p></div><SiteIcon name="arrow" />
        </a>
      </div>
      <div class="mcn-foundation"><strong>MCN Boot</strong><span>约定配置</span><span>条件装配</span><span>依赖管理</span><strong>Spring 生态</strong></div>
    </section>

    <section class="mcn-guides mcn-wrap" aria-labelledby="mcn-guides-title">
      <div><p class="mcn-eyebrow">04 / FIND YOUR WAY</p><h2 id="mcn-guides-title">从第一步，<br />到更深一层。</h2><p>每一个清晰的起点，<br />都值得一份清晰的文档。</p></div>
      <div class="mcn-guide-list">
        <a :href="withBase('/intro/ready.html')"><span>开始之前</span><strong>准备你的开发环境</strong><SiteIcon name="arrow" /></a>
        <a :href="withBase('/guide/quick-start.html')"><span>动手构建</span><strong>运行第一个应用</strong><SiteIcon name="arrow" /></a>
        <a :href="withBase('/spring/core.html')"><span>理解原理</span><strong>走进 Spring Boot</strong><SiteIcon name="arrow" /></a>
      </div>
    </section>

    <section class="mcn-callout mcn-wrap" aria-labelledby="mcn-callout-title">
      <div><p class="mcn-eyebrow">LESS SETUP. MORE BUILDING.</p><h2 id="mcn-callout-title">下一个项目，从这里开始。</h2><p>准备好 Java 与 Maven，跟着文档构建你的第一个应用。</p></div>
      <a class="mcn-button" :href="withBase('/guide/quick-start.html')">开始构建 <SiteIcon name="arrow" /></a>
    </section>
  </div>
</template>
