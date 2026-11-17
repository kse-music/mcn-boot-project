/* Local design proposals. Product examples follow the repository README, not legacy docs. */
const concepts = {
  forest: { name: '深林工作台', letter: 'A', description: 'A / 深林工作台 — 深色薄荷绿 · 工程现场感 · 接近 ServerDeck 的品牌气质' },
  paper: { name: '暖白技术刊物', letter: 'B', description: 'B / 暖白技术刊物 — 暖白陶土橙 · 编辑式排版 · 阅读与品牌并重' },
  orbit: { name: '午夜架构图谱', letter: 'C', description: 'C / 午夜架构图谱 — 墨蓝电紫 · 架构可视化 · 突出技术体系' },
};

const icons = {
  arrow: '<path d="M4 12h15m-6-6 6 6-6 6"/>',
  external: '<path d="M14 4h6v6M20 4l-10 10M10 4H4v16h16v-6"/>',
  code: '<path d="m8 7-5 5 5 5m8-10 5 5-5 5m-3-14-2 18"/>',
  layers: '<path d="m12 3 10 5-10 5L2 8l10-5Zm-10 9 10 5 10-5M2 16l10 5 10-5"/>',
  sliders: '<path d="M4 4v6m0 4v6M12 4v10m0 4v2M20 4v2m0 4v10M1 10h6m2 4h6m2-8h6"/>',
  book: '<path d="M12 6v15M12 6C9 3 4 3 2 4v15c4-1 7-1 10 2 3-3 6-3 10-2V4c-3-1-7-1-10 2Z"/>',
  check: '<path d="m5 12 4 4L19 6"/>',
  search: '<circle cx="10" cy="10" r="6"/><path d="m15 15 5 5"/>',
  github: '<path d="M9 19c-5 1-5-3-7-3m14 6v-4c0-1-.3-2-1-2 4-.4 7-2 7-6 0-2-.5-3-2-4 .3-1 .3-3-.2-4 0 0-2 0-4 2-3-1-5-1-8 0C6 2 4 2 4 2c-.5 1-.5 3-.2 4C2.5 7 2 8 2 10c0 4 3 6 7 6-.7.4-1 1-1 2v4"/>',
  terminal: '<path d="m4 5 6 6-6 6m9 0h7"/>',
  menu: '<path d="M4 6h16M4 12h16M4 18h16"/>',
  copy: '<rect x="8" y="8" width="12" height="13" rx="2"/><path d="M15 8V3H3v13h5"/>',
};
const icon = (name, cls = '') => `<svg class="icon ${cls}" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${icons[name] || icons.code}</svg>`;
const escapeHtml = text => text.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#39;');
const parentCode = `<parent>
    <groupId>cn.hiboot.mcn</groupId>
    <artifactId>mcn-boot-starter-parent</artifactId>
    <version>4.1.0</version>
</parent>`;
const dependencyCode = `<dependencies>
    <dependency>
        <groupId>cn.hiboot.mcn</groupId>
        <artifactId>mcn-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
</dependencies>`;
const bomCode = `<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>cn.hiboot.mcn</groupId>
            <artifactId>mcn-boot-dependencies</artifactId>
            <version>4.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>`;

function syntax(code, language = 'xml') {
  if (language !== 'xml') return escapeHtml(code);
  return code.split(/(<[^>]*>)/g).map(part => part.startsWith('<') ? `<span class="syn-tag">${escapeHtml(part)}</span>` : escapeHtml(part)).join('');
}

function codeBlock(code, label = 'pom.xml', language = 'xml') {
  return `<div class="code-block"><div class="code-head"><span>${icon('code')}${label}</span><button type="button" class="copy-button" data-copy aria-label="复制 ${label} 代码">${icon('copy')}<span>复制</span></button></div><pre><code data-language="${language}">${syntax(code, language)}</code></pre></div>`;
}

let state;
function readLocation() {
  const params = new URLSearchParams(location.search);
  return { concept: Object.hasOwn(concepts, params.get('concept')) ? params.get('concept') : 'forest', view: params.get('view') === 'docs' ? 'docs' : 'home', doc: Object.hasOwn(articles, params.get('doc')) ? params.get('doc') : 'quick-start' };
}
function route(view, doc = 'quick-start', hash = '') {
  return `?concept=${state.concept}&view=${view}${view === 'docs' ? `&doc=${doc}` : ''}${hash ? `#${hash}` : ''}`;
}
function docLink(title, doc = 'quick-start', cls = '') {
  return `<a class="${cls}" href="${route('docs', doc)}" data-route>${title}${icon('arrow')}</a>`;
}
function header() {
  return `<a class="skip-link" href="#main-content">跳至正文</a>
  <header class="site-header wrap">
    <a class="brand" href="${route('home')}" data-route aria-label="MCN Boot 首页"><span class="brand-mark" aria-hidden="true">m</span><span>MCN<span class="brand-weight"> Boot</span></span></a>
    <nav class="site-nav" aria-label="主导航"><a class="${state.view === 'home' ? 'active' : ''}" href="${route('home')}" data-route>概览</a><a class="${state.view === 'docs' ? 'active' : ''}" href="${route('docs')}" data-route>开发文档</a><a href="${route('home', '', 'ecosystem')}" data-route>组件生态</a></nav>
    <div class="header-actions"><button type="button" class="search-trigger" data-search>${icon('search')}<span>搜索文档</span><kbd>Ctrl K</kbd></button><a class="github-link" href="https://github.com/kse-music/mcn-boot-project" target="_blank" rel="noopener noreferrer" aria-label="在新标签页查看 GitHub 源码">${icon('github')}</a><button class="mobile-menu" type="button" data-menu aria-label="展开导航" aria-expanded="false">${icon('menu')}</button></div>
  </header>`;
}

function footer() {
  return `<footer class="site-footer wrap"><a class="footer-brand" href="${route('home')}" data-route>MCN Boot<span>把基础打好，让业务向前。</span></a><div>${docLink('开发文档')}<a href="https://github.com/kse-music/mcn-boot-project" target="_blank" rel="noopener noreferrer">GitHub ${icon('external')}</a><span>HiBoot · 开源 Java 工具包</span></div></footer>`;
}

function technologyStrip() {
  return `<div class="technology-strip"><div class="wrap"><span>构建在你熟悉的生态之上</span><b>Java <small>17+</small></b><b>Spring Boot</b><b>Spring Cloud</b><b>Servlet <small>/</small> Reactive</b></div></div>`;
}

function editor() {
  return `<div class="editor-scene"><div class="scene-caption"><span>YOUR NEXT APPLICATION</span><span>01 / START SIMPLE</span></div><div class="editor-window"><div class="editor-titlebar"><span class="window-dots"><i></i><i></i><i></i></span><span>my-application</span><span>~/workspace</span></div><div class="editor-body"><div class="editor-files"><span>EXPLORER</span><p>⌄ my-application</p><p class="file-indent">⌄ src</p><p class="file-indent second">java</p><p class="file-indent second">resources</p><p class="selected-file">${icon('code')} pom.xml</p></div><div class="editor-code"><div class="editor-tab">${icon('code')} pom.xml <span>×</span></div><div class="code-lines"><div class="line-numbers" aria-hidden="true">01<br>02<br>03<br>04<br>05<br>06<br>07<br>08<br>09<br>10<br>11<br>12</div><pre><code><span class="syn-comment">&lt;!-- 少一些配置，多一些业务。 --&gt;</span>
${syntax(parentCode)}

<span class="syn-comment">&lt;!-- 引入所需组件，按条件启用。 --&gt;</span>
${syntax('<dependency>\n    <groupId>cn.hiboot.mcn</groupId>\n    <artifactId>mcn-spring-boot-starter</artifactId>\n</dependency>')}</code></pre></div></div></div><div class="editor-status"><span>⌘ main</span><span>XML <span class="status-dot"></span> 约定已就绪</span></div></div><div class="assembly-note"><span class="assembly-icon">${icon('layers')}</span><div><strong>恰好需要的基础设施</strong><span>依赖引入 → 条件匹配 → 自动装配</span></div>${icon('check')}</div></div>`;
}

const capabilities = [
  { number: '01', icon: 'sliders', title: '约定，开箱即用。', text: '统一响应、异常处理、参数校验与跨域配置，让每一个项目都有熟悉的起点。', detail: 'JSON / Validation / Web', doc: 'auto-config' },
  { number: '02', icon: 'layers', title: '组件，按需组合。', text: '从数据库、缓存到对象存储，只引入所需的依赖，再让条件装配接好余下的部分。', detail: 'JPA / MyBatis / Redis / MinIO', doc: 'dependencies' },
  { number: '03', icon: 'code', title: '生态，保持熟悉。', text: '沿用 Spring Boot 与 Spring Cloud 的开发方式，支持 Servlet、Reactive 和非 Web 应用。', detail: 'Spring Boot / Spring Cloud', doc: 'quick-start' },
];
function capabilityGrid() {
  return `<div class="capability-grid">${capabilities.map(item => `<article><div class="cap-top">${icon(item.icon)}<span>${item.number}</span></div><h3>${item.title}</h3><p>${item.text}</p><div class="cap-bottom"><span>${item.detail}</span><a href="${route('docs', item.doc)}" data-route aria-label="了解${item.title}">${icon('arrow')}</a></div></article>`).join('')}</div>`;
}
function ecosystem() {
  return `<section id="ecosystem" class="ecosystem-section wrap"><div class="section-heading"><div><p class="eyebrow">COMPOSABLE BY NATURE</p><h2>基础设施，各就其位。</h2></div><p>保留熟悉的技术选择。<br>让通用能力在应用之下自然衔接。</p></div><div class="ecosystem-map"><div class="ecosystem-root"><span class="brand-mark">m</span><div><strong>你的业务应用</strong><span>专注业务逻辑与领域模型</span></div></div><div class="ecosystem-spine" aria-hidden="true"></div><div class="ecosystem-modules"><div><b>Web 与接口</b><span>Servlet · WebFlux · OpenAPI</span></div><div><b>数据与存储</b><span>JDBC · JPA · MyBatis · MongoDB</span></div><div><b>云与协作</b><span>Redis · MinIO · OpenFeign</span></div></div><div class="ecosystem-foundation"><b>MCN Boot</b><span>约定配置</span><span>条件装配</span><span>依赖管理</span><b>Spring 生态</b></div></div></section>`;
}
function startSection() {
  return `<section class="start-section wrap"><div><p class="eyebrow">LESS SETUP. MORE BUILDING.</p><h2>下一个项目，从这里开始。</h2><p>准备好 Java 与 Maven，跟着文档构建你的第一个应用。</p></div>${docLink('开始构建', 'quick-start', 'button primary')}</section>`;
}

function forestHome() {
  return `<main id="main-content"><section class="forest-hero wrap"><div class="hero-copy"><a class="release-pill" href="${route('docs')}" data-route><span class="status-dot"></span> MCN BOOT 4.1.0 <span class="release-divider">/</span> 为 Spring 开发者而生 ${icon('arrow')}</a><p class="eyebrow">LESS REPETITION. MORE CREATION.</p><h1>把重复的工程，<br><em>交给约定。</em></h1><p class="hero-description">让基础设施就位，让业务先行。<br>基于 Spring Boot 与 Spring Cloud 的 Java 开发工具包，<br class="desktop-break">把通用能力，变成每一个项目的自然起点。</p><div class="hero-actions">${docLink('开始构建', 'quick-start', 'button primary')}<a class="button secondary" href="#ecosystem">探索组件 ${icon('arrow')}</a></div><div class="hero-footnote"><span>${icon('check')} 按需引入</span><span>${icon('check')} 条件装配</span><span>${icon('check')} 开源可扩展</span></div></div>${editor()}</section>${technologyStrip()}<section class="features-section wrap"><div class="section-heading"><div><p class="eyebrow">01 / A BETTER STARTING POINT</p><h2>少一点重复。<em>多一点专注。</em></h2></div><p>从单体应用到微服务，<br>把每次都要做的事，做好一次。</p></div>${capabilityGrid()}</section>${ecosystem()}${startSection()}</main>`;
}

function paperDiagram() {
  return `<div class="paper-diagram"><div class="diagram-topline"><span>THE ANATOMY OF A GOOD START</span><span>图 / 01</span></div><div class="paper-stack"><div class="paper-layer layer-app"><span>01</span><strong>你的下一个好想法</strong><small>Application / Business</small></div><div class="paper-join"><span></span><b>留出专注业务的空间</b><span></span></div><div class="paper-layer layer-mcn"><span>02</span><strong>MCN Boot</strong><small>约定 · 装配 · 扩展</small></div><div class="paper-layer layer-spring"><span>03</span><strong>Spring 生态</strong><small>Boot / Cloud / Data</small></div><div class="paper-layer layer-java"><span>04</span><strong>Java 17+</strong><small>A familiar foundation.</small></div></div><div class="diagram-caption"><span>依赖可以很轻。<br>基础要足够扎实。</span><span class="diagram-arrow" aria-hidden="true">↗</span></div></div>`;
}

function paperHome() {
  return `<main id="main-content"><section class="paper-hero wrap"><div class="paper-issue"><span>MCN BOOT / 开发者的基础工具集</span><span>JAVA · SPRING · OPEN SOURCE</span></div><div class="paper-hero-grid"><div class="hero-copy"><p class="eyebrow">GOOD FOUNDATIONS. GREAT POSSIBILITIES.</p><h1>好工程，<br>从<em>少一点重复</em><br>开始。</h1><p class="hero-description">把通用配置留给工具，把创造留给开发者。<br>用熟悉的 Spring 生态，构建有条理的 Java 应用。</p><div class="hero-actions">${docLink('翻开开发指南', 'quick-start', 'button primary')}<a class="text-link" href="#ecosystem">看看它如何工作 ${icon('arrow')}</a></div><span class="paper-version">MCN Boot 4.1.0 <span>—</span> 为每一次认真构建。</span></div>${paperDiagram()}</div></section><section class="paper-intro wrap"><div><p class="eyebrow">01 / START WITH THE ESSENTIALS</p><h2>一份依赖。<br>一个更好的起点。</h2><p>把配置、组件与规范放回它们该在的位置。<br>从清晰的约定开始，沿着自己的业务生长。</p>${docLink('阅读快速开始', 'quick-start', 'text-link')}</div>${codeBlock(parentCode)}</section><section class="features-section wrap"><div class="section-heading"><div><p class="eyebrow">02 / THOUGHTFULLY COMPOSED</p><h2>好的工具，<em>让工作更从容。</em></h2></div><p>不改变你熟悉的开发方式，<br>在重复之处，往前一步。</p></div>${capabilityGrid()}</section>${ecosystem()}${startSection()}</main>`;
}

function orbitGraph() {
  return `<div class="orbit-graph" role="img" aria-label="MCN Boot 架构示意：业务应用连接 MCN Boot，按需组合 Web、数据、云服务和 API 文档，底层使用 Spring Boot、Spring Cloud 与 Java。"><div class="graph-caption"><span>THE APPLICATION CONSTELLATION</span><span class="graph-key"><i></i> 按需组合</span></div><div class="graph-canvas"><svg class="graph-lines" viewBox="0 0 900 290" fill="none" aria-hidden="true" preserveAspectRatio="none"><path d="M450 42V125M450 125H120V207M450 125H340V207M450 125H560V207M450 125H780V207"/><circle cx="450" cy="125" r="5"/></svg><div class="graph-app">${icon('code')}<span>YOUR APPLICATION</span></div><div class="graph-center"><span class="brand-mark">m</span><strong>MCN Boot</strong><span>CONVENTION INTO CAPABILITY</span></div><div class="graph-nodes"><div>${icon('sliders')}<strong>Web 基础设施</strong><small>Servlet / Reactive</small></div><div>${icon('layers')}<strong>数据与存储</strong><small>JPA / MyBatis / Redis</small></div><div>${icon('terminal')}<strong>云与服务</strong><small>Cloud / OpenFeign</small></div><div>${icon('book')}<strong>API 文档</strong><small>OpenAPI / Knife4j</small></div></div></div><div class="graph-base"><span>BUILT ON</span><b>Spring Boot</b><span>+</span><b>Spring Cloud</b><span>+</span><b>Java 17+</b></div></div>`;
}

function orbitHome() {
  return `<main id="main-content"><section class="orbit-hero wrap"><div class="orbit-grid-texture" aria-hidden="true"></div><a class="release-pill" href="${route('docs')}" data-route><span class="status-dot"></span> MCN BOOT 4.1.0 <span class="release-divider">/</span> COMPOSE YOUR NEXT APPLICATION ${icon('arrow')}</a><p class="eyebrow">THE FOUNDATION BEHIND WHAT'S NEXT</p><h1>连接基础能力。<br><em>释放业务创造力。</em></h1><p class="hero-description">以约定组织复杂，以组合保持灵活。<br>让 Spring Boot 与 Spring Cloud 的通用能力，在你的应用中自然连接。</p><div class="hero-actions">${docLink('构建你的应用', 'quick-start', 'button primary')}<a class="button secondary" href="#ecosystem">探索技术图谱 ${icon('arrow')}</a></div>${orbitGraph()}</section><section class="features-section wrap"><div class="section-heading"><div><p class="eyebrow">01 / ONE FOUNDATION. MANY POSSIBILITIES.</p><h2>一个基础。<em>多种可能。</em></h2></div><p>从通用配置到跨服务协作，<br>用模块组合你的应用基础。</p></div>${capabilityGrid()}</section>${ecosystem()}${startSection()}</main>`;
}

const articles = {
  'quick-start': {
    title: '快速开始', category: '开始构建', subtitle: '从一份 Maven 配置开始，构建你的第一个 MCN Boot 应用。',
    sections: [['requirements', '开始之前'], ['parent', '引入父模块'], ['starter', '添加应用依赖'], ['run', '启动应用']],
    body: () => `<div class="prerequisite" id="requirements"><div class="prerequisite-icon">${icon('check')}</div><div><strong>开始之前</strong><p>准备 Java 17+ 和 Maven 3.6.3+，以及你习惯的 Java 开发环境。</p></div><span class="small-tag">环境准备</span></div><section id="parent" class="doc-section"><h2><span class="step-number">01</span> 引入父模块</h2><p>在项目的 <code>pom.xml</code> 中继承 <code>mcn-boot-starter-parent</code>，复用统一的依赖与插件管理。</p>${codeBlock(parentCode)}<div class="doc-tip">${icon('book')}<p>已有自己的父 POM？可以通过 ${docLink('导入 BOM', 'dependencies', 'inline-link')} 管理依赖版本。</p></div></section><section id="starter" class="doc-section"><h2><span class="step-number">02</span> 添加应用依赖</h2><p>添加 MCN Boot starter，再选择应用所需的 Spring Boot starter。下面以 Servlet 应用为例。</p>${codeBlock(dependencyCode)}<p class="muted">可选能力在对应依赖存在时按条件启用；只引入应用实际需要的组件。</p></section><section id="run" class="doc-section"><h2><span class="step-number">03</span> 启动应用</h2><p>创建带有 <code>@SpringBootApplication</code> 的启动类，设置应用名称，然后运行应用。</p>${codeBlock('spring:\n  application:\n    name: demo', 'application.yml', 'yaml')}${codeBlock('mvn spring-boot:run', 'Terminal', 'shell')}<div class="doc-tip">${icon('book')}<p>如果当前源码版本尚未发布到 Maven 仓库，先在 MCN Boot 项目根目录执行 <code>mvn install</code>。</p></div></section>`,
    next: 'dependencies', previous: null,
  },
  dependencies: {
    title: '依赖管理', category: '开始构建', subtitle: '通过父 POM 或 BOM，让依赖版本保持一致，让组件选择保持自由。',
    sections: [['choose', '选择接入方式'], ['bom', '导入 BOM'], ['components', '选择组件']],
    body: () => `<section id="choose" class="doc-section"><h2>选择接入方式</h2><p>新应用可以继承 <code>mcn-boot-starter-parent</code>。如果项目已有父 POM，则导入 <code>mcn-boot-dependencies</code>。</p><div class="doc-table-wrap"><table><thead><tr><th>接入方式</th><th>适用情况</th><th>提供的能力</th></tr></thead><tbody><tr><td>Parent POM</td><td>可以继承 MCN 父模块</td><td>依赖与插件管理</td></tr><tr><td>BOM</td><td>需要保留现有父 POM</td><td>依赖版本管理</td></tr></tbody></table></div></section><section id="bom" class="doc-section"><h2>导入 BOM</h2><p>将以下配置加入 <code>dependencyManagement</code>。随后按需声明 starter 与组件依赖。</p>${codeBlock(bomCode)}<div class="doc-tip">${icon('book')}<p>BOM 管理依赖版本。使用 BOM 时，请自行保留并配置应用所需的 Maven 插件。</p></div></section><section id="components" class="doc-section"><h2>选择组件</h2><p>通用入口为 <code>mcn-spring-boot-starter</code>。API 文档可按需引入 <code>mcn-swagger3</code>，集成 springdoc-openapi 与 Knife4j。</p>${codeBlock('<dependency>\n    <groupId>cn.hiboot.mcn</groupId>\n    <artifactId>mcn-swagger3</artifactId>\n</dependency>')}${docLink('继续了解自动配置', 'auto-config', 'text-link')}</section>`,
    next: 'auto-config', previous: 'quick-start',
  },
  'auto-config': {
    title: '自动配置', category: '核心能力', subtitle: '以约定减少重复配置，用条件装配让通用能力恰好就位。',
    sections: [['principle', '如何工作'], ['capabilities', '能力分组'], ['boundary', '使用边界']],
    body: () => `<section id="principle" class="doc-section"><h2>如何工作</h2><p>MCN Boot 基于 Spring Boot 的自动配置机制，依据 classpath 与应用属性按条件启用组件。</p><div class="doc-flow"><span>引入依赖</span>${icon('arrow')}<span>条件匹配</span>${icon('arrow')}<span>自动装配</span></div><p>不同应用可以使用 Servlet、Reactive 或非 Web 开发模式。实际生效的配置取决于应用依赖、属性和运行环境。</p></section><section id="capabilities" class="doc-section"><h2>能力分组</h2><div class="doc-table-wrap"><table><thead><tr><th>模块</th><th>主要职责</th></tr></thead><tbody><tr><td><code>mcn-boot-core</code></td><td>共享模型、Jackson 支持、工具与客户端抽象</td></tr><tr><td><code>mcn-boot-autoconfigure</code></td><td>通用 Spring Boot 自动配置</td></tr><tr><td><code>mcn-cloud-autoconfigure</code></td><td>Spring Cloud、客户端与安全相关自动配置</td></tr><tr><td><code>mcn-swagger3</code></td><td>OpenAPI 与 Knife4j 集成</td></tr></tbody></table></div></section><section id="boundary" class="doc-section"><h2>使用边界</h2><p>可选能力仅在对应依赖存在时激活。根据应用需要选择组件，具体条件与属性应结合对应模块配置查看。</p><div class="doc-tip">${icon('book')}<p>本页展示文档的视觉组织方式。正式升级时，应逐项核对当前源码中的条件与配置属性。</p></div>${docLink('查看依赖接入方式', 'dependencies', 'text-link')}</section>`,
    previous: 'dependencies', next: null,
  },
};

function docs() {
  const article = articles[state.doc];
  const sideItem = (id, number) => `<a class="${state.doc === id ? 'selected' : ''}" href="${route('docs', id)}" data-route ${state.doc === id ? 'aria-current="page"' : ''}><span>${number}</span>${articles[id].title}${state.doc === id ? '<span class="sidebar-dot"></span>' : ''}</a>`;
  return `<main id="main-content" class="docs-layout wrap"><aside class="docs-sidebar" aria-label="文档导航"><div class="sidebar-title">${icon('book')} 开发文档 <span>4.1.0</span></div><button class="sidebar-search" type="button" data-search>${icon('search')} 搜索文档 <kbd>⌘ K</kbd></button><div class="sidebar-group"><h2>开始构建</h2>${sideItem('quick-start', '01')}${sideItem('dependencies', '02')}</div><div class="sidebar-group"><h2>核心能力</h2>${sideItem('auto-config', '03')}</div><div class="sidebar-outline"><h2>文档体系规划</h2><span>通用功能 / 扩展组件</span><span>Spring 原理 / 参考资料</span><small>其余章节在正式实施时迁入</small></div><a class="sidebar-source" href="https://github.com/kse-music/mcn-boot-project" target="_blank" rel="noopener noreferrer">${icon('github')} 探索项目源码 ${icon('external')}</a><div class="sidebar-note">从约定出发，<br>为业务留出空间。<span>BUILD WITH MCN.</span></div></aside><article class="doc-article"><div class="doc-breadcrumb">开发文档 <span>/</span> ${article.category} <span>/</span> <b>${article.title}</b></div><div class="doc-topline"><p class="eyebrow">${state.doc === 'quick-start' ? 'GET UP AND BUILD' : state.doc === 'dependencies' ? 'COMPOSE YOUR DEPENDENCIES' : 'CONVENTION INTO CAPABILITY'}</p><span class="doc-version">MCN BOOT 4.1.0</span></div><h1>${article.title}</h1><p class="doc-subtitle">${article.subtitle}</p><div class="doc-meta"><span>${icon('code')} Java / Maven</span><span>基于当前项目 README</span></div>${article.body()}<div class="doc-pagination">${article.previous ? `<a href="${route('docs', article.previous)}" data-route><small>← 上一篇</small><strong>${articles[article.previous].title}</strong></a>` : '<span></span>'}${article.next ? `<a href="${route('docs', article.next)}" data-route><small>下一篇 →</small><strong>${articles[article.next].title}</strong></a>` : '<span></span>'}</div><p class="doc-disclosure">设计预览 · 本轮提供 3 篇示例，用于比较阅读与交互体验。</p></article><aside class="doc-toc" aria-label="本页目录"><h2>本页内容</h2>${article.sections.map(([id, title], index) => `<a href="#${id}"><span>0${index + 1}</span>${title}</a>`).join('')}<div class="toc-help"><span class="brand-mark">m</span><strong>从这里，开始构建。</strong><p>熟悉的 Spring。<br>更从容的开发。</p><a href="${route('home')}" data-route>回到产品概览 ${icon('arrow')}</a></div></aside></main>`;
}

function render({ scroll = false } = {}) {
  const concept = concepts[state.concept];
  document.body.dataset.concept = state.concept;
  document.body.dataset.view = state.view;
  document.querySelector('#concept-description').textContent = concept.description;
  document.querySelectorAll('button[data-concept]').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.concept === state.concept)));
  document.querySelectorAll('button[data-view]').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.view === state.view)));
  document.querySelector('#site').innerHTML = header() + (state.view === 'docs' ? docs() : { forest: forestHome, paper: paperHome, orbit: orbitHome }[state.concept]()) + footer();
  document.title = `${concept.letter} · ${concept.name} / ${state.view === 'home' ? '官网首页' : articles[state.doc].title} — MCN Boot`;
  if (scroll) window.scrollTo({ top: 0, behavior: 'instant' });
  if (location.hash) requestAnimationFrame(() => document.getElementById(decodeURIComponent(location.hash.slice(1)))?.scrollIntoView());
}

function navigate(url) {
  history.pushState(null, '', url);
  state = readLocation();
  render({ scroll: true });
}

const dialog = document.querySelector('#search-dialog');
const searchInput = document.querySelector('#search-input');
let searchReturnFocus;
function searchResults() {
  const query = searchInput.value.trim().toLocaleLowerCase();
  const matches = Object.entries(articles).filter(([, article]) => `${article.title} ${article.category} ${article.subtitle} ${article.sections.flat().join(' ')} ${article.body().replace(/<[^>]*>/g, ' ')}`.toLocaleLowerCase().includes(query));
  document.querySelector('#search-results').innerHTML = matches.length ? matches.map(([id, article]) => `<a class="search-result" href="${route('docs', id)}" data-route data-search-result>${icon('book')}<span><strong>${article.title}</strong><small>${article.subtitle}</small></span>${icon('arrow')}</a>`).join('') : '<p class="search-empty">没有匹配的预览文档。试试“依赖”“Maven”或“自动配置”。</p>';
}
function openSearch() {
  searchReturnFocus = document.activeElement;
  searchInput.value = '';
  searchResults();
  dialog.showModal();
  searchInput.focus();
}
dialog.addEventListener('close', () => {
  if (searchReturnFocus?.isConnected) searchReturnFocus.focus();
});
searchInput.addEventListener('input', searchResults);
document.addEventListener('click', async event => {
  const conceptButton = event.target.closest('button[data-concept]');
  if (conceptButton) {
    const params = new URLSearchParams(location.search);
    params.set('concept', conceptButton.dataset.concept);
    navigate(`?${params}`);
    return;
  }
  const viewButton = event.target.closest('button[data-view]');
  if (viewButton) { navigate(route(viewButton.dataset.view, state.doc)); return; }
  if (event.target.closest('[data-search]')) { openSearch(); return; }
  if (event.target.closest('[data-close-search]')) { dialog.close(); return; }
  const menu = event.target.closest('[data-menu]');
  if (menu) {
    const expanded = menu.getAttribute('aria-expanded') !== 'true';
    menu.setAttribute('aria-expanded', String(expanded));
    document.querySelector('.site-header').classList.toggle('menu-open', expanded);
    return;
  }
  const copy = event.target.closest('[data-copy]');
  if (copy) {
    try {
      await navigator.clipboard.writeText(copy.closest('.code-block').querySelector('code').textContent);
      copy.querySelector('span').textContent = '已复制';
      document.querySelector('#announcement').textContent = '代码已复制';
    } catch {
      copy.querySelector('span').textContent = '请选择代码复制';
      document.querySelector('#announcement').textContent = '浏览器未允许写入剪贴板，请选择代码手动复制。';
    }
    return;
  }
  const link = event.target.closest('a[data-route]');
  if (link && !event.metaKey && !event.ctrlKey && !event.shiftKey && !event.altKey && event.button === 0) {
    event.preventDefault();
    const fromSearch = link.hasAttribute('data-search-result');
    if (dialog.open) dialog.close();
    navigate(link.href);
    if (fromSearch) {
      const heading = document.querySelector('.doc-article h1');
      heading.tabIndex = -1;
      heading.focus({ preventScroll: true });
    }
  }
});
document.addEventListener('keydown', event => {
  if (event.key === 'Escape' && dialog.open) {
    event.preventDefault();
    dialog.close();
    return;
  }
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault();
    if (dialog.open) dialog.close(); else openSearch();
  }
});
window.addEventListener('popstate', () => { state = readLocation(); render({ scroll: true }); });
state = readLocation();
render();
