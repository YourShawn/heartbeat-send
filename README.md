# Heartbeat Send · 听/存心跳

Listen to a pulse. Keep it. Optionally share a link. **Not a medical device.**

用自定义曲线、情境问卷（规则或 OpenAI）或可穿戴占位接口，保存一记可在浏览器本地播放的脉搏。**不是医疗器械，不能用于诊断或治疗。**

MIT licensed. Stack: Java 17+ / Spring Boot 3.4, MySQL 8, React (Vite + TypeScript).

---

## English

### What it is

A small listening room for heartbeat *programs* (BPM + timbre + curve):

| Source | Library tag | Sensor? |
| --- | --- | --- |
| **(A) Custom** BPM / timbre / curve | 自定义 Custom | Always non-sensor |
| **(B) Questionnaire** → rule engine, or OpenAI if `OPENAI_API_KEY` is set | 生成 Generated | Always labeled **non-sensor** |
| **(C) Wearable placeholder** + mock capture | 真测 Measured | Mock is **not** a live sensor (`sensor_origin = 0`) |

Playback happens **in the browser** (Web Audio). The server stores parameters only, never audio files.

### How to run

MySQL must be reachable **only on localhost or a private network**. Do not expose it on a public IP or put a public host in committed config.

**Docker Compose** (backend + frontend). Default `docker compose up` does **not** start MySQL; point `MYSQL_HOST` at an external instance:

- Same host as Docker: `host.docker.internal` or the host gateway (Linux Compose maps `host.docker.internal` via `extra_hosts`).
- Another container on an internal Compose network: that service hostname (for example `mysql` with the optional profile below).
- Production: a private hostname or localhost in uncommitted `.env`.

```bash
cp .env.example .env
# Compose cannot use 127.0.0.1 inside the backend container for host MySQL.
# Set MYSQL_HOST=host.docker.internal in .env (or the host gateway).
docker compose up --build
```

Open http://localhost (UI) and http://localhost/swagger-ui.html (OpenAPI).  
Demo login: `demo` / `demo123`.

Optional solo local demo (bundled MySQL on the Compose network, published only on `127.0.0.1:3306`):

```bash
# Set MYSQL_HOST=mysql in .env, then:
docker compose --profile bundled-mysql up --build
```

**Local (two terminals)** after MySQL 8 is up on loopback (`MYSQL_HOST=127.0.0.1`) and `heartbeat_send` exists:

```bash
# terminal 1
cd backend && mvn spring-boot:run

# terminal 2
cd frontend && npm install && npm run dev
```

UI: http://localhost:5173 · API: http://localhost:8080 · OpenAPI: http://localhost:8080/swagger-ui.html

### Tests

```bash
cd backend && mvn test          # JUnit smoke against MySQL (heartbeat_send_test)
./scripts/smoke.sh              # HTTP smoke against a running API
```

GitHub Actions runs the Maven smoke job with a MySQL 8 service.

### Configuration

See `.env.example`. Important variables:

- `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DATABASE` / `MYSQL_USER` / `MYSQL_PASSWORD` — database. `MYSQL_HOST` in committed examples is `127.0.0.1` only; production uses a private hostname or localhost in uncommitted `.env`. Never a public IP.
- `JWT_SECRET` — HS256 secret (≥ 32 characters in production)
- `OPENAI_API_KEY` / `OPENAI_BASE_URL` / `OPENAI_MODEL` — optional; empty key ⇒ deterministic rules
- `PUBLIC_BASE_URL` — used when minting share URLs (committed examples are `http://localhost:...` only)

### Architecture

```
frontend/          React SPA (responsive)
backend/           Spring Boot MVC
  common/          JWT, security, OpenAPI, errors
  auth/            login + demo seed user
  heartbeat/       library, custom, synth, wearable mock, share
docs/schema.md     column-level data dictionary (PK/FK)
```

Classic layering: **controller → service → repository**. Flyway owns the schema (`ddl-auto=validate`).

---

## 中文

### 这是什么

一间很小的「听心跳」房间。保存的是可播放的脉搏方案（BPM、音色、曲线），不是病历。

| 来源 | 收藏标签 | 是否传感器 |
| --- | --- | --- |
| **（A）自定义** 心率 / 音色 / 曲线 | 自定义 | 永远非传感器 |
| **（B）情境问卷** → 规则引擎；若配置了 `OPENAI_API_KEY` 则走兼容 OpenAI 的模型 | 生成 | **必须**标明非传感器 |
| **（C）可穿戴占位接口** + 模拟采集 | 真测 | 当前模拟 **不是** 真实设备读数 |

声音只在 **本机浏览器** 合成。服务端只存参数，不存音频文件。界面全程有非医疗声明。

### 如何运行

MySQL **只能**通过本机回环或私有网络访问，不要把数据库绑到公网 IP，也不要在已提交的配置里写公网主机名。

**Docker Compose**（默认只起 backend + frontend，需外部 MySQL）：

- 与 Docker 同机：`MYSQL_HOST=host.docker.internal` 或宿主机网关
- 同一 Compose 内部网络：用内部服务名（例如可选 profile 下的 `mysql`）
- 生产：未提交的 `.env` 里写私有主机名或 localhost

```bash
cp .env.example .env
# 容器内访问宿主机 MySQL 请把 MYSQL_HOST 设为 host.docker.internal
docker compose up --build
```

打开 http://localhost ，演示账号 `demo` / `demo123`。

可选的单机演示（启动捆绑 MySQL，主机侧仅监听 `127.0.0.1:3306`）：

```bash
# 在 .env 中设置 MYSQL_HOST=mysql，然后：
docker compose --profile bundled-mysql up --build
```

**本地开发：** 先在本机回环上准备 MySQL 8 与库 `heartbeat_send`（`MYSQL_HOST=127.0.0.1`），再分别启动 `backend` 与 `frontend`（见英文小节命令）。

### 测试

```bash
cd backend && mvn test
./scripts/smoke.sh
```

### 配置与架构

环境变量见 `.env.example`。无 `OPENAI_API_KEY` 时自动回退到确定性规则。表结构与每一列的含义见 [`docs/schema.md`](docs/schema.md)。

模块划分：`common`（JWT / 安全 / OpenAPI）、`auth`（登录与演示用户）、`heartbeat`（收藏、自定义、生成、可穿戴占位、分享）。

---

## Disclaimer / 免责声明

This software is a listening and archiving toy. It is **not** FDA/NMPA cleared, **not** a heart-rate monitor, and **must not** be used to make medical decisions.

本软件仅供聆听与保存。它不是医疗器械，不能替代医师、监护仪或任何诊疗建议。
