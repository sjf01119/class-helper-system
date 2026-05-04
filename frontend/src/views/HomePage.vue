<template>
  <div class="home-page">
    <header class="home-header">
      <div class="brand-block">
        <div class="brand-mark">L</div>
        <div class="brand-copy">
          <div class="brand-title">学习辅助系统</div>
          <div class="brand-subtitle">Learning Assistant Platform</div>
        </div>
      </div>

      <div class="header-actions">
        <el-button text class="header-button" @click="goRegister">注册</el-button>
        <el-button type="primary" round class="header-login" @click="goLogin">登录</el-button>
      </div>
    </header>

    <main class="hero-section">
      <section class="hero-copy">
        <div class="hero-badge">智慧教学协同平台</div>
        <h1>连接课程、作业与成绩管理的统一入口</h1>
        <p>
          面向管理员、教师与学生提供一体化学习协同体验，让课程组织、作业批改与学习反馈更清晰、更高效。
        </p>

        <div class="hero-actions">
          <el-button type="primary" size="large" round class="hero-primary" @click="goLogin">
            立即登录
          </el-button>
          <el-button size="large" round class="hero-secondary" @click="goRegister">
            学生注册
          </el-button>
        </div>

        <div class="hero-metrics">
          <div class="metric-card">
            <strong>课程管理</strong>
            <span>课程内容、学习资料统一组织</span>
          </div>
          <div class="metric-card">
            <strong>作业协同</strong>
            <span>发布、提交、批改流程清晰可追踪</span>
          </div>
          <div class="metric-card">
            <strong>成绩反馈</strong>
            <span>统计概览直观，学习表现一目了然</span>
          </div>
        </div>
      </section>

      <section class="hero-visual" aria-label="学习辅助系统平台展示">
        <div class="visual-card visual-card--main">
          <div class="visual-tag">教学看板</div>
          <div class="visual-title">围绕学习过程构建高效协作</div>
          <div class="visual-grid">
            <div class="visual-panel">
              <span class="panel-label">课程进度</span>
              <strong>12 门</strong>
              <p>当前学期课程统一管理，支持资料与章节联动</p>
            </div>
            <div class="visual-panel">
              <span class="panel-label">作业批改</span>
              <strong>86%</strong>
              <p>待批改、已提交、已完成状态实时同步</p>
            </div>
            <div class="visual-panel visual-panel--wide">
              <span class="panel-label">学习反馈</span>
              <div class="progress-line">
                <span class="progress-fill"></span>
              </div>
              <p>成绩统计、班级公告、课程资源在同一平台内闭环协同</p>
            </div>
          </div>
        </div>

        <div class="floating-card floating-card--left">
          <span>课程发布</span>
          <strong>高效组织教学内容</strong>
        </div>
        <div class="floating-card floating-card--right">
          <span>公告通知</span>
          <strong>重要信息准确触达</strong>
        </div>
      </section>
    </main>

    <section class="feature-section">
      <div class="section-heading">
        <span>核心能力</span>
        <h2>贴合教学场景的关键模块</h2>
        <p>保持简洁流程设计，同时覆盖课程、作业、成绩与通知等高频场景。</p>
      </div>

      <div class="feature-grid">
        <article class="feature-card">
          <div class="feature-icon">01</div>
          <h3>课程管理</h3>
          <p>支持课程信息展示、资料归档与章节组织，帮助教学内容更有条理。</p>
        </article>
        <article class="feature-card">
          <div class="feature-icon">02</div>
          <h3>作业批改</h3>
          <p>覆盖作业发布、提交进度追踪和批改反馈，提升教师处理效率。</p>
        </article>
        <article class="feature-card">
          <div class="feature-icon">03</div>
          <h3>成绩统计</h3>
          <p>统一查看成绩概览、完成情况与学习反馈，让数据辅助教学决策。</p>
        </article>
      </div>
    </section>

    <footer class="home-footer">
      <span>© 2026 学习辅助系统</span>
      <span>Class Helper System. All rights reserved.</span>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const openAuthPage = (path: '/login' | '/register') => {
  // 已登录状态下点击首页认证按钮时，先清理本地登录态，避免被守卫立即打回首页。
  if (localStorage.getItem('token')) {
    userStore.logout()
  }
  router.push(path)
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
  color: #1f2937;
  background:
    radial-gradient(circle at top left, rgba(91, 153, 255, 0.2), transparent 28%),
    radial-gradient(circle at top right, rgba(58, 123, 213, 0.14), transparent 24%),
    linear-gradient(180deg, #f7faff 0%, #eef4ff 45%, #f4f8ff 100%);
}

.home-header {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
  padding: 18px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  position: sticky;
  top: 16px;
  z-index: 20;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(121, 159, 227, 0.16);
  box-shadow: 0 14px 34px rgba(39, 81, 154, 0.1);
  backdrop-filter: blur(16px);
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 16px;
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
  background: linear-gradient(135deg, #2f74ff 0%, #69a2ff 100%);
  box-shadow: 0 14px 28px rgba(47, 116, 255, 0.24);
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.01em;
  color: #1f2937;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 12px;
  letter-spacing: 0.12em;
  color: #94a3b8;
  text-transform: uppercase;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 50;
}

.header-button,
.header-login,
.hero-primary,
.hero-secondary {
  min-height: 44px;
  cursor: pointer;
  position: relative;
  z-index: 60;
  pointer-events: auto;
}

.header-button {
  color: #436089;
  border-radius: 999px;
  transition: color 0.22s ease, background-color 0.22s ease, transform 0.22s ease;
}

.header-login {
  padding: 0 22px;
  background: linear-gradient(135deg, #2f74ff 0%, #4f8cff 100%);
  border: none;
  box-shadow: 0 12px 24px rgba(47, 116, 255, 0.22);
  transition: transform 0.22s ease, box-shadow 0.22s ease, filter 0.22s ease;
}

.hero-section {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
  padding: 42px 0 30px;
  display: grid;
  grid-template-columns: minmax(0, 1.02fr) minmax(360px, 0.98fr);
  gap: 36px;
  align-items: center;
}

.hero-copy {
  padding: 22px 0;
  position: relative;
  z-index: 5;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 16px;
  border-radius: 999px;
  color: #2f66cb;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(86, 141, 243, 0.18);
  box-shadow: 0 10px 24px rgba(113, 149, 214, 0.1);
}

.hero-copy h1 {
  margin: 22px 0 20px;
  font-size: clamp(36px, 4vw, 56px);
  line-height: 1.12;
  font-weight: 820;
  letter-spacing: -0.025em;
  color: #1f2937;
}

.hero-copy p {
  max-width: 640px;
  margin: 0;
  font-size: 17px;
  line-height: 1.92;
  color: #6b7280;
}

.hero-actions {
  margin-top: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  position: relative;
  z-index: 6;
}

.hero-primary {
  padding: 0 28px;
  border: none;
  background: linear-gradient(135deg, #2f74ff 0%, #5c96ff 100%);
  box-shadow: 0 18px 32px rgba(47, 116, 255, 0.24);
}

.hero-secondary {
  padding: 0 28px;
  color: #2f66cb;
  border-color: rgba(76, 131, 235, 0.28);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 10px 22px rgba(67, 96, 137, 0.08);
}

.hero-metrics {
  margin-top: 34px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.metric-card,
.feature-card,
.visual-card,
.floating-card {
  border: 1px solid rgba(105, 150, 232, 0.16);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 40px rgba(42, 82, 152, 0.1);
  backdrop-filter: blur(14px);
}

.metric-card {
  padding: 18px 18px 16px;
  border-radius: 22px;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
}

.metric-card strong {
  display: block;
  font-size: 16px;
  color: #1f2937;
}

.metric-card span {
  display: block;
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.8;
  color: #94a3b8;
}

.hero-visual {
  position: relative;
  min-height: 560px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.visual-card--main {
  width: 100%;
  max-width: 520px;
  padding: 30px;
  border-radius: 34px;
  box-shadow: 0 28px 56px rgba(41, 85, 160, 0.16);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.visual-tag {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #2f66cb;
  background: rgba(47, 116, 255, 0.1);
}

.visual-title {
  margin-top: 16px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.35;
  color: #1f2937;
}

.visual-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.visual-panel {
  padding: 20px;
  border-radius: 24px;
  background: linear-gradient(180deg, #f9fbff 0%, #eff5ff 100%);
  border: 1px solid rgba(103, 145, 223, 0.15);
}

.visual-panel--wide {
  grid-column: 1 / -1;
}

.panel-label {
  display: block;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: #94a3b8;
  text-transform: uppercase;
}

.visual-panel strong {
  display: block;
  margin-top: 10px;
  font-size: 30px;
  color: #1f2937;
}

.visual-panel p {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1.85;
  color: #6b7280;
}

.progress-line {
  position: relative;
  margin-top: 18px;
  height: 10px;
  border-radius: 999px;
  background: rgba(122, 153, 210, 0.18);
  overflow: hidden;
}

.progress-fill {
  display: block;
  width: 74%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2f74ff 0%, #72a7ff 100%);
}

.floating-card {
  position: absolute;
  max-width: 220px;
  padding: 18px 20px;
  border-radius: 22px;
  pointer-events: none;
}

.floating-card span {
  display: block;
  font-size: 12px;
  color: #94a3b8;
}

.floating-card strong {
  display: block;
  margin-top: 8px;
  line-height: 1.6;
  color: #1f2937;
}

.floating-card--left {
  left: 6px;
  bottom: 84px;
}

.floating-card--right {
  top: 58px;
  right: 0;
}

.feature-section {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
  padding: 64px 0 34px;
}

.section-heading {
  max-width: 720px;
}

.section-heading span {
  color: #2f66cb;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  display: inline-block;
  margin-bottom: 10px;
}

.section-heading h2 {
  margin: 0 0 16px;
  font-size: clamp(28px, 3vw, 40px);
  line-height: 1.28;
  color: #1f2937;
}

.section-heading p {
  margin: 0;
  font-size: 16px;
  line-height: 1.85;
  color: #6b7280;
}

.feature-grid {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.feature-card {
  padding: 30px 24px;
  border-radius: 28px;
  box-shadow: 0 20px 42px rgba(42, 82, 152, 0.1);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background-color 0.22s ease;
}

.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #2f74ff 0%, #6ca7ff 100%);
  box-shadow: 0 14px 28px rgba(47, 116, 255, 0.22);
}

.feature-card h3 {
  margin: 18px 0 10px;
  font-size: 20px;
  color: #1f2937;
}

.feature-card p {
  margin: 0;
  font-size: 14px;
  line-height: 1.95;
  color: #6b7280;
}

.home-footer {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
  padding: 30px 0 40px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px 20px;
  font-size: 13px;
  color: #94a3b8;
  border-top: 1px solid rgba(121, 159, 227, 0.14);
}

@media (hover: hover) {
  .metric-card,
  .feature-card,
  .floating-card,
  .visual-panel,
  .header-login,
  .hero-primary,
  .hero-secondary {
    transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  }

  .header-button:hover {
    color: #2f66cb;
    background: rgba(47, 116, 255, 0.08);
    transform: translateY(-1px);
  }

  .metric-card:hover,
  .feature-card:hover,
  .floating-card:hover,
  .visual-panel:hover {
    transform: translateY(-4px);
    box-shadow: 0 22px 40px rgba(42, 82, 152, 0.14);
    border-color: rgba(76, 131, 235, 0.26);
  }

  .metric-card:hover {
    background: rgba(240, 247, 255, 0.96);
  }

  .feature-card:hover {
    background: rgba(247, 250, 255, 0.98);
  }

  .visual-card--main:hover {
    transform: translateY(-6px);
    box-shadow: 0 34px 62px rgba(41, 85, 160, 0.2);
    border-color: rgba(76, 131, 235, 0.22);
  }

  .header-login:hover,
  .hero-primary:hover,
  .hero-secondary:hover {
    transform: translateY(-2px);
    box-shadow: 0 18px 30px rgba(47, 116, 255, 0.24);
  }

  .hero-secondary:hover {
    background: rgba(240, 247, 255, 0.96);
  }
}

.header-button:focus-visible,
.header-login:focus-visible,
.hero-primary:focus-visible,
.hero-secondary:focus-visible {
  outline: 3px solid rgba(61, 124, 255, 0.24);
  outline-offset: 2px;
}

@media (max-width: 1100px) {
  .hero-section {
    grid-template-columns: 1fr;
  }

  .hero-copy {
    padding-top: 8px;
  }

  .hero-visual {
    min-height: auto;
    padding-bottom: 18px;
  }

  .floating-card--left {
    left: 16px;
    bottom: 30px;
  }

  .floating-card--right {
    top: 12px;
    right: 16px;
  }

  .feature-grid,
  .hero-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .home-header,
  .hero-section,
  .feature-section,
  .home-footer {
    width: min(100% - 32px, 1180px);
  }

  .home-header {
    flex-direction: column;
    align-items: flex-start;
    top: 12px;
    padding: 16px 18px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .hero-copy h1 {
    font-size: 34px;
  }

  .hero-actions {
    width: 100%;
  }

  .hero-primary,
  .hero-secondary {
    flex: 1 1 180px;
  }

  .feature-grid,
  .hero-metrics,
  .visual-grid {
    grid-template-columns: 1fr;
  }

  .visual-card--main {
    padding: 22px;
    border-radius: 28px;
  }

  .floating-card {
    position: static;
    max-width: none;
    margin-top: 14px;
  }

  .hero-visual {
    display: block;
  }

  .home-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .home-header,
  .hero-section,
  .feature-section,
  .home-footer {
    width: calc(100% - 24px);
  }

  .brand-title {
    font-size: 20px;
  }

  .home-header {
    border-radius: 20px;
  }

  .hero-copy p,
  .section-heading p {
    font-size: 15px;
  }

  .visual-title {
    font-size: 24px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .metric-card,
  .feature-card,
  .floating-card,
  .visual-panel,
  .header-login,
  .hero-primary,
  .hero-secondary {
    transition: none;
  }
}
</style>
