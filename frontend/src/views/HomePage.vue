<template>
  <div class="home-page">
    <header class="home-header">
      <div class="header-shell">
        <button class="brand-block" type="button" @click="scrollToSection('top')">
          <span class="brand-mark">L</span>
          <span class="brand-copy">
            <span class="brand-title">学习辅助系统</span>
            <span class="brand-subtitle">Learning Assistant Platform</span>
          </span>
        </button>

        <nav class="header-nav" aria-label="首页导航">
          <button class="nav-link" type="button" @click="scrollToSection('top')">首页</button>
          <button class="nav-link" type="button" @click="scrollToSection('features')">功能介绍</button>
          <button class="nav-link" type="button" @click="scrollToSection('about')">关于我们</button>
        </nav>

        <div class="header-actions">
          <el-button text class="header-action header-register" @click="goRegister">注册</el-button>
          <el-button type="primary" round class="header-action header-login" @click="goLogin">登录</el-button>
        </div>
      </div>
    </header>

    <main>
      <section id="top" class="hero-section">
        <div class="hero-copy">
          <div class="hero-badge">教学协同平台</div>
          <h1>让教学更高效，让学习更清晰</h1>
          <p>
            一站式课程、作业、成绩管理平台，为师生提供全流程教学协同体验，
            让课程组织、作业流转、数据反馈与公告通知在同一平台高效闭环。
          </p>

          <div class="hero-actions">
            <el-button type="primary" size="large" round class="hero-primary" @click="goLogin">
              立即登录
            </el-button>
            <el-button size="large" round class="hero-secondary" @click="scrollToSection('features')">
              了解更多
            </el-button>
          </div>

          <div class="hero-summary">
            <div class="summary-card">
              <span>面向角色</span>
              <strong>管理员 / 教师 / 学生</strong>
            </div>
            <div class="summary-card">
              <span>协同链路</span>
              <strong>课程、作业、成绩、公告</strong>
            </div>
            <div class="summary-card">
              <span>平台特征</span>
              <strong>简洁、清晰、可追踪</strong>
            </div>
          </div>
        </div>

        <div class="hero-visual" aria-label="教育平台看板展示">
          <div class="hero-orb hero-orb--left" />
          <div class="hero-orb hero-orb--right" />

          <div class="dashboard-card">
            <div class="dashboard-head">
              <div>
                <div class="dashboard-kicker">教学看板</div>
                <div class="dashboard-title">课程与作业协同视图</div>
              </div>
              <div class="dashboard-status">实时同步</div>
            </div>

            <div class="dashboard-grid">
              <div class="panel-card panel-card--wide">
                <div class="panel-top">
                  <span>课程进度</span>
                  <strong>统一管理</strong>
                </div>
                <div class="panel-bars">
                  <span class="bar bar--primary" />
                  <span class="bar bar--secondary" />
                  <span class="bar bar--light" />
                </div>
              </div>

              <div class="panel-card">
                <span class="panel-label">作业批改</span>
                <strong>状态同步</strong>
                <p>发布、提交、批改状态实时联动</p>
              </div>

              <div class="panel-card">
                <span class="panel-label">公告通知</span>
                <strong>精准触达</strong>
                <p>系统与班级信息准确通知到位</p>
              </div>

              <div class="panel-card panel-card--wide">
                <div class="panel-top">
                  <span>学习反馈</span>
                  <strong>清晰可视</strong>
                </div>
                <div class="progress-track">
                  <span class="progress-value" />
                </div>
                <div class="progress-meta">
                  <span>完成情况清晰可视</span>
                  <span>成绩变化一目了然</span>
                </div>
              </div>
            </div>
          </div>

          <div class="floating-badge floating-badge--left">
            <span>课程管理</span>
            <strong>章节与资料统一维护</strong>
          </div>
          <div class="floating-badge floating-badge--right">
            <span>成绩反馈</span>
            <strong>统计结果辅助教学决策</strong>
          </div>
        </div>
      </section>

      <section id="features" class="feature-section">
        <div class="section-heading">
          <span>功能介绍</span>
          <h2>围绕教学主流程设计的四个关键模块</h2>
          <p>兼顾管理员治理、教师教学与学生学习体验，让平台入口更统一，操作更清晰。</p>
        </div>

        <div class="feature-grid">
          <article class="feature-card">
            <div class="feature-icon feature-icon--blue">
              <el-icon><Reading /></el-icon>
            </div>
            <h3>课程管理</h3>
            <p>课程信息、章节与资料一站式管理，帮助教学内容稳定沉淀、统一组织。</p>
          </article>

          <article class="feature-card">
            <div class="feature-icon feature-icon--indigo">
              <el-icon><DocumentChecked /></el-icon>
            </div>
            <h3>作业协同</h3>
            <p>发布、提交、批改全流程追踪，让教师处理更高效，学生进度更清楚。</p>
          </article>

          <article class="feature-card">
            <div class="feature-icon feature-icon--sky">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <h3>成绩反馈</h3>
            <p>成绩统计、完成情况与趋势概览统一呈现，让学习反馈更加直观透明。</p>
          </article>

          <article class="feature-card">
            <div class="feature-icon feature-icon--cyan">
              <el-icon><Bell /></el-icon>
            </div>
            <h3>公告通知</h3>
            <p>系统与班级信息精准触达，重要公告可分角色、分班级快速下发与查看。</p>
          </article>
        </div>
      </section>

      <section class="data-section">
        <div class="section-heading section-heading--center">
          <span>平台教学数据</span>
          <h2>用简洁数据看板快速感知平台运行状态</h2>
          <p>延续首页教学看板的视觉语言，以更清晰的数字和进度展示平台覆盖与协同效果。</p>
        </div>

        <div class="data-layout">
          <div class="data-grid">
            <article class="data-card">
              <span class="data-label">课程管理</span>
              <strong>统一维护</strong>
              <p>支持课程内容、章节资料与状态统一维护</p>
            </article>

            <article class="data-card">
              <span class="data-label">作业协同</span>
              <strong>流程清晰</strong>
              <p>批改进度持续同步，帮助教师快速追踪教学完成情况</p>
            </article>

            <article class="data-card">
              <span class="data-label">班级管理</span>
              <strong>协同统一</strong>
              <p>平台面向不同班级场景提供统一的信息协同入口</p>
            </article>
          </div>

          <div class="insight-card">
            <div class="insight-row">
              <span>课程管理概览</span>
            </div>
            <div class="insight-progress">
              <span class="insight-progress__value insight-progress__value--primary" />
            </div>

            <div class="insight-row">
              <span>作业协同概览</span>
            </div>
            <div class="insight-progress">
              <span class="insight-progress__value insight-progress__value--secondary" />
            </div>

            <div class="insight-row">
              <span>公告通知概览</span>
            </div>
            <div class="insight-progress">
              <span class="insight-progress__value insight-progress__value--accent" />
            </div>
          </div>
        </div>
      </section>

      <section id="about" class="about-section">
        <div class="about-card">
          <span class="about-kicker">关于平台</span>
          <h2>面向校园教学场景的一站式学习协同入口</h2>
          <p>
            面向管理员、教师与学生的校园学习协同平台，致力于让教学组织、
            作业批改与学习反馈更高效、更透明。
          </p>

          <div class="about-footer">
            <span>© 2026 学习辅助系统 | 一站式教学协同平台</span>
            <span>保持清晰流程、统一入口与稳定协作体验</span>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { Bell, DataAnalysis, DocumentChecked, Reading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { clearActiveRoleScope } from '@/utils/auth'

const router = useRouter()

const openAuthPage = (path: '/login' | '/register') => {
  clearActiveRoleScope()
  router.push(path)
}

const scrollToSection = (id: 'top' | 'features' | 'about') => {
  const element = document.getElementById(id)
  element?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const goLogin = () => {
  openAuthPage('/login')
}

const goRegister = () => {
  openAuthPage('/register')
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  color: #183153;
  background:
    radial-gradient(circle at 0% 0%, rgba(88, 146, 255, 0.22), transparent 28%),
    radial-gradient(circle at 100% 10%, rgba(90, 177, 255, 0.18), transparent 24%),
    linear-gradient(180deg, #f7fbff 0%, #edf4ff 48%, #f5f9ff 100%);
}

.home-header {
  position: sticky;
  top: 0;
  z-index: 40;
  padding: 14px 0;
  backdrop-filter: blur(16px);
}

.header-shell,
.hero-section,
.feature-section,
.data-section,
.about-section {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
}

.header-shell {
  min-height: 78px;
  padding: 14px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  border-radius: 24px;
  border: 1px solid rgba(113, 155, 226, 0.18);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 16px 38px rgba(34, 73, 144, 0.1);
}

.brand-block,
.nav-link {
  border: none;
  background: transparent;
  cursor: pointer;
}

.brand-block {
  padding: 0;
  display: flex;
  align-items: center;
  gap: 14px;
  text-align: left;
}

.brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #2f74ff 0%, #67a4ff 100%);
  box-shadow: 0 14px 26px rgba(47, 116, 255, 0.24);
}

.brand-copy {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2f49;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #8aa0c1;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  min-height: 42px;
  padding: 0 16px;
  border-radius: 999px;
  color: #4d6489;
  font-size: 14px;
  transition: background-color 0.24s ease, color 0.24s ease, transform 0.24s ease;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-action,
.hero-primary,
.hero-secondary {
  min-height: 46px;
  position: relative;
  z-index: 1;
}

.header-register {
  color: #41618f;
}

.header-login,
.hero-primary {
  border: none;
  background: linear-gradient(135deg, #2f74ff 0%, #4f8cff 52%, #76b0ff 100%);
  box-shadow: 0 16px 28px rgba(47, 116, 255, 0.22);
}

.hero-section {
  min-height: calc(100vh - 106px);
  padding: 42px 0 56px;
  display: grid;
  grid-template-columns: minmax(0, 1.04fr) minmax(380px, 0.96fr);
  gap: 34px;
  align-items: center;
}

.hero-copy {
  padding: 22px 0;
}

.hero-badge,
.dashboard-status,
.about-kicker,
.section-heading span {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2f67cc;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(86, 141, 243, 0.18);
  box-shadow: 0 10px 24px rgba(80, 125, 203, 0.1);
}

.hero-copy h1 {
  margin: 22px 0 18px;
  max-width: 660px;
  font-size: clamp(40px, 4.7vw, 64px);
  line-height: 1.08;
  letter-spacing: -0.03em;
  color: #192b46;
}

.hero-copy p {
  max-width: 640px;
  margin: 0;
  font-size: 17px;
  line-height: 1.95;
  color: #637594;
}

.hero-actions {
  margin-top: 32px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.hero-primary,
.hero-secondary {
  padding: 0 28px;
}

.hero-secondary {
  color: #2f67cc;
  background: rgba(255, 255, 255, 0.86);
  border-color: rgba(88, 138, 230, 0.24);
  box-shadow: 0 10px 24px rgba(62, 100, 166, 0.08);
}

.hero-summary {
  margin-top: 34px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.summary-card,
.feature-card,
.data-card,
.insight-card,
.dashboard-card,
.floating-badge,
.about-card {
  border: 1px solid rgba(105, 150, 232, 0.16);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 20px 42px rgba(42, 82, 152, 0.1);
  backdrop-filter: blur(14px);
}

.summary-card {
  padding: 18px 18px 16px;
  border-radius: 22px;
}

.summary-card span {
  display: block;
  font-size: 12px;
  color: #8aa0c1;
}

.summary-card strong {
  display: block;
  margin-top: 10px;
  font-size: 16px;
  line-height: 1.65;
  color: #22324e;
}

.hero-visual {
  position: relative;
  min-height: 620px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(10px);
  pointer-events: none;
}

.hero-orb--left {
  top: 40px;
  left: 30px;
  width: 150px;
  height: 150px;
  background: rgba(105, 166, 255, 0.18);
}

.hero-orb--right {
  right: 18px;
  bottom: 56px;
  width: 180px;
  height: 180px;
  background: rgba(61, 124, 255, 0.14);
}

.dashboard-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 540px;
  padding: 30px;
  border-radius: 34px;
  box-shadow: 0 28px 58px rgba(39, 81, 154, 0.16);
}

.dashboard-head,
.panel-top,
.progress-meta,
.insight-row,
.about-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.dashboard-kicker {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #8aa0c1;
}

.dashboard-title {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.35;
  color: #20314d;
}

.dashboard-status {
  white-space: nowrap;
}

.dashboard-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel-card {
  padding: 20px;
  border-radius: 24px;
  background: linear-gradient(180deg, #fbfdff 0%, #eef5ff 100%);
  border: 1px solid rgba(103, 145, 223, 0.15);
}

.panel-card--wide {
  grid-column: 1 / -1;
}

.panel-top span,
.panel-label,
.progress-meta,
.data-label,
.data-card p,
.insight-row span,
.about-footer,
.about-card p,
.section-heading p {
  color: #667892;
}

.panel-label,
.data-label {
  display: block;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.panel-card strong,
.data-card strong {
  display: block;
  margin-top: 10px;
  font-size: 32px;
  color: #1d2e49;
}

.panel-card p,
.data-card p {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1.85;
}

.panel-bars {
  margin-top: 18px;
  display: grid;
  gap: 10px;
}

.bar,
.progress-value,
.insight-progress__value {
  display: block;
  height: 10px;
  border-radius: 999px;
}

.bar--primary {
  width: 92%;
  background: linear-gradient(90deg, #2f74ff 0%, #78b0ff 100%);
}

.bar--secondary {
  width: 74%;
  background: linear-gradient(90deg, rgba(47, 116, 255, 0.26) 0%, rgba(120, 176, 255, 0.8) 100%);
}

.bar--light {
  width: 58%;
  background: rgba(114, 156, 223, 0.24);
}

.progress-track,
.insight-progress {
  margin-top: 14px;
  height: 10px;
  border-radius: 999px;
  background: rgba(114, 156, 223, 0.18);
  overflow: hidden;
}

.progress-value {
  width: 76%;
  background: linear-gradient(90deg, #2f74ff 0%, #76afff 100%);
}

.progress-meta {
  margin-top: 12px;
  font-size: 12px;
}

.floating-badge {
  position: absolute;
  padding: 18px 20px;
  max-width: 220px;
  border-radius: 22px;
  pointer-events: none;
}

.floating-badge span {
  display: block;
  font-size: 12px;
  color: #8aa0c1;
}

.floating-badge strong {
  display: block;
  margin-top: 8px;
  line-height: 1.65;
  color: #20314d;
}

.floating-badge--left {
  left: 6px;
  bottom: 88px;
}

.floating-badge--right {
  top: 64px;
  right: 2px;
}

.feature-section,
.data-section,
.about-section {
  padding: 40px 0 36px;
}

.section-heading {
  max-width: 760px;
}

.section-heading--center {
  margin: 0 auto;
  text-align: center;
}

.section-heading h2,
.about-card h2 {
  margin: 16px 0;
  font-size: clamp(30px, 3.2vw, 42px);
  line-height: 1.24;
  color: #1c2d47;
}

.section-heading p,
.about-card p {
  margin: 0;
  font-size: 16px;
  line-height: 1.9;
}

.feature-grid {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.feature-card {
  padding: 28px 24px;
  border-radius: 28px;
}

.feature-icon {
  width: 54px;
  height: 54px;
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  box-shadow: 0 14px 28px rgba(47, 116, 255, 0.2);
}

.feature-icon--blue {
  background: linear-gradient(135deg, #2f74ff 0%, #6faaff 100%);
}

.feature-icon--indigo {
  background: linear-gradient(135deg, #3f69ff 0%, #7aa1ff 100%);
}

.feature-icon--sky {
  background: linear-gradient(135deg, #2d8bff 0%, #6fc3ff 100%);
}

.feature-icon--cyan {
  background: linear-gradient(135deg, #308dff 0%, #6acfff 100%);
}

.feature-card h3 {
  margin: 18px 0 10px;
  font-size: 20px;
  color: #1f304a;
}

.feature-card p {
  margin: 0;
  font-size: 14px;
  line-height: 1.9;
  color: #667892;
}

.data-layout {
  margin-top: 28px;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(300px, 0.8fr);
  gap: 20px;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.data-card,
.insight-card,
.about-card {
  border-radius: 30px;
}

.data-card {
  padding: 24px;
}

.insight-card {
  padding: 26px;
}

.insight-row {
  font-size: 14px;
}

.insight-row strong {
  color: #1d2e49;
}

.insight-progress + .insight-row {
  margin-top: 18px;
}

.insight-progress__value--primary {
  width: 82%;
  background: linear-gradient(90deg, #2f74ff 0%, #76afff 100%);
}

.insight-progress__value--secondary {
  width: 86%;
  background: linear-gradient(90deg, #4a83ff 0%, #8ab9ff 100%);
}

.insight-progress__value--accent {
  width: 91%;
  background: linear-gradient(90deg, #2f96ff 0%, #7ed4ff 100%);
}

.about-card {
  padding: 34px;
}

.about-footer {
  margin-top: 26px;
  padding-top: 20px;
  border-top: 1px solid rgba(111, 152, 226, 0.16);
  font-size: 13px;
}

@media (hover: hover) {
  .header-shell,
  .nav-link,
  .header-login,
  .hero-primary,
  .hero-secondary,
  .summary-card,
  .dashboard-card,
  .feature-card,
  .data-card,
  .insight-card,
  .about-card,
  .panel-card {
    transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease, background-color 0.24s ease;
  }

  .header-shell:hover {
    background: rgba(255, 255, 255, 0.82);
    box-shadow: 0 20px 44px rgba(34, 73, 144, 0.12);
  }

  .nav-link:hover {
    color: #2f67cc;
    background: rgba(47, 116, 255, 0.08);
    transform: translateY(-1px);
  }

  .header-login:hover,
  .hero-primary:hover,
  .hero-secondary:hover {
    transform: translateY(-2px);
    box-shadow: 0 20px 34px rgba(47, 116, 255, 0.24);
  }

  .summary-card:hover,
  .dashboard-card:hover,
  .feature-card:hover,
  .data-card:hover,
  .insight-card:hover,
  .about-card:hover,
  .panel-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 24px 48px rgba(42, 82, 152, 0.14);
    border-color: rgba(76, 131, 235, 0.24);
  }

  .hero-secondary:hover {
    background: rgba(244, 249, 255, 0.98);
  }
}

.brand-block:focus-visible,
.nav-link:focus-visible,
.header-register:focus-visible,
.header-login:focus-visible,
.hero-primary:focus-visible,
.hero-secondary:focus-visible {
  outline: 3px solid rgba(61, 124, 255, 0.22);
  outline-offset: 3px;
}

@media (max-width: 1180px) {
  .hero-section {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .hero-visual {
    min-height: auto;
  }

  .feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .data-layout,
  .data-grid {
    grid-template-columns: 1fr;
  }

  .floating-badge--left {
    left: 18px;
    bottom: 34px;
  }

  .floating-badge--right {
    right: 18px;
    top: 20px;
  }
}

@media (max-width: 860px) {
  .header-shell {
    flex-wrap: wrap;
    justify-content: center;
  }

  .header-nav {
    order: 3;
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }

  .header-actions {
    margin-left: auto;
  }

  .hero-copy h1 {
    font-size: 38px;
  }

  .hero-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .header-shell,
  .hero-section,
  .feature-section,
  .data-section,
  .about-section {
    width: min(100% - 32px, 1180px);
  }

  .home-header {
    padding: 12px 0;
  }

  .header-shell {
    padding: 16px 18px;
    border-radius: 22px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .hero-section {
    padding-top: 28px;
  }

  .hero-actions {
    width: 100%;
  }

  .hero-primary,
  .hero-secondary {
    flex: 1 1 180px;
  }

  .dashboard-card,
  .about-card {
    padding: 24px;
    border-radius: 26px;
  }

  .dashboard-grid,
  .feature-grid {
    grid-template-columns: 1fr;
  }

  .floating-badge {
    position: static;
    max-width: none;
    margin-top: 14px;
  }

  .about-footer,
  .dashboard-head,
  .panel-top,
  .progress-meta {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .header-shell,
  .hero-section,
  .feature-section,
  .data-section,
  .about-section {
    width: calc(100% - 24px);
  }

  .brand-title {
    font-size: 20px;
  }

  .brand-subtitle {
    letter-spacing: 0.08em;
  }

  .hero-copy h1 {
    font-size: 34px;
  }

  .hero-copy p,
  .section-heading p,
  .about-card p {
    font-size: 15px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .header-shell,
  .nav-link,
  .header-login,
  .hero-primary,
  .hero-secondary,
  .summary-card,
  .dashboard-card,
  .feature-card,
  .data-card,
  .insight-card,
  .about-card,
  .panel-card {
    transition: none;
  }
}
</style>
