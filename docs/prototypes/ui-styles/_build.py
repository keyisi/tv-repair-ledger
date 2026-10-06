#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 4 套视觉风格的高保真原型（独立 HTML）+ 一个对比看板 index.html。

用法：python3 _build.py
输入：_base.css（结构与组件） + _theme-*.css（各套变量） + _screens.html（5 屏内容）
输出：01-清爽蓝.html … 04-商务数据.html、index.html
"""

import pathlib
import re

HERE = pathlib.Path(__file__).resolve().parent

BASE = (HERE / "_base.css").read_text(encoding="utf-8")
SCREENS = (HERE / "_screens.html").read_text(encoding="utf-8")


def screen_block(name: str) -> str:
    m = re.search(
        r"<!-- SCREEN:%s START -->(.*?)<!-- SCREEN:%s END -->" % (name, name),
        SCREENS,
        re.S,
    )
    if not m:
        raise SystemExit("找不到屏幕块: %s" % name)
    return m.group(1).strip()


THEMES = [
    {
        "key": "clarity",
        "file": "01-清爽蓝.html",
        "name": "清爽蓝 Clarity Blue",
        "tagline": "延续现有视觉，圆角与留白现代化升级",
        "desc": "沿用当前 App 的靛蓝主色与白卡体系，加大圆角、拉开间距、统一字号层级。改动最小，老用户零学习成本。",
        "chips": ["#145C74 主色", "圆角 12", "白卡 + 浅灰底", "高对比易读"],
    },
    {
        "key": "night",
        "file": "02-夜间深色.html",
        "name": "夜间深色 Night Ledger",
        "tagline": "收工后记账不刺眼，数字更跳",
        "desc": "深蓝灰底 + 亮青主色 + 荧光绿利润色。卡片靠底色分层而非阴影，OLED 屏省电，低光环境长时间看不疲劳。",
        "chips": ["#57C7E8 主色", "圆角 14", "深底 #0E1621", "无阴影分层"],
    },
    {
        "key": "warm",
        "file": "03-暖色手艺.html",
        "name": "暖色手艺 Warm Craft",
        "tagline": "纸质账本的亲和感，弱化财务软件距离",
        "desc": "米白纸感底 + 赭石主色 + 橄榄绿利润。字号与行距略放大、圆角更柔，面向中老年客户的上门场景更讨喜。",
        "chips": ["#9A5B28 主色", "圆角 16", "米白 #FBF5EC", "行距 1.7"],
    },
    {
        "key": "exec",
        "file": "04-商务数据.html",
        "name": "商务数据 Data Executive",
        "tagline": "高信息密度，一屏扫更多条",
        "desc": "直角卡片、细边框、去阴影、小字号 + 等宽数字。牺牲一点亲和力换密度，适合单量大、需要快速核对数字的老板。",
        "chips": ["#2563EB 主色", "圆角 6", "无阴影", "等宽数字"],
    },
]

PAGE = """<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>{name} · 维修记账高保真原型</title>
<style>
{base}

/* ================= 主题变量 ================= */
{theme}
</style>
</head>
<body>
<div class="board">
  <header class="headline">
    <div>
      <h1>{name}</h1>
      <p>{desc}</p>
    </div>
    <div class="tokens">
      {chips}
    </div>
  </header>
  <section class="screens">
{screens}
  </section>
</div>
</body>
</html>
"""

for t in THEMES:
    css = (HERE / ("_theme-%s.css" % t["key"])).read_text(encoding="utf-8")
    html = PAGE.format(
        name=t["name"],
        desc=t["desc"],
        chips="\n      ".join(
            '<span class="token%s">%s</span>' % (" solid" if i == 0 else "", c)
            for i, c in enumerate(t["chips"])
        ),
        base=BASE,
        theme=css,
        screens=SCREENS.strip(),
    )
    out = HERE / t["file"]
    out.write_text(html, encoding="utf-8")
    print("生成 %-22s %7.1f KB" % (t["file"], out.stat().st_size / 1024))

# ---------- 对比看板 ----------
home = screen_block("home")
compare_cells = []
for t in THEMES:
    cell = home.replace(
        '<p class="screen-label">01 首页经营概览</p>',
        '<p class="screen-label">%s</p>' % t["name"],
    )
    compare_cells.append('<div class="theme-%s">%s</div>' % (t["key"], cell))

ROWS = [
    ("主色 primary", lambda t: t["chips"][0]),
    ("页面底色", lambda t: t["chips"][2]),
    ("卡片圆角", lambda t: t["chips"][1]),
    ("设计倾向", lambda t: t["chips"][3]),
]

table_rows = "\n".join(
    "<tr><td>%s</td>%s</tr>"
    % (
        label,
        "".join("<td>%s</td>" % fn(t) for t in THEMES),
    )
    for label, fn in ROWS
)

INDEX = """<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>维修记账 · 4 套视觉风格对比</title>
<style>
{base}
</style>
</head>
<body>
<div class="board">
  <header class="headline">
    <div>
      <h1>维修记账 · 4 套视觉风格对比</h1>
      <p>
        同一批真实数据（104 单 / 收入 ¥39,130 / 利润 ¥25,850）下的四套视觉方案，
        覆盖首页、台账、按月统计、录入表单、备份与常用项 5 个核心屏。
        下面的对比只看首页，点开任意一套可以看到完整 5 屏。
      </p>
    </div>
    <div class="tokens">
      <span class="token solid">数据来自 v1.15 真机</span>
      <span class="token">2026-10-05</span>
    </div>
  </header>

  <section class="compare">
{cells}
  </section>

  <div style="margin-top:38px">
    <h2 style="margin:0 0 4px;font-size:20px">风格取向速查</h2>
    <p class="muted">要点差异一眼看完，选定后可直接照抄色值到 Compose 主题。</p>
    <table class="tokentable">
      <tr><th>维度</th>{heads}</tr>
{rows}
    </table>
  </div>

  <div style="margin-top:26px">
    <h2 style="margin:0 0 4px;font-size:20px">完整原型</h2>
    <p class="muted">每套都是独立文件，含 5 屏，可直接双击在浏览器打开。</p>
    <div class="picklist">
{links}
    </div>
  </div>
</div>
</body>
</html>
"""

links = "\n".join(
    '<a href="{f}">{n} →</a>'.format(f=t["file"], n=t["name"]) for t in THEMES
)
heads = "".join("<th>%s</th>" % t["name"] for t in THEMES)

index_html = INDEX.format(
    base=BASE,
    cells="\n".join(compare_cells),
    heads=heads,
    rows=table_rows,
    links=links,
)
(HERE / "index.html").write_text(index_html, encoding="utf-8")
print("生成 %-22s %7.1f KB" % ("index.html", (HERE / "index.html").stat().st_size / 1024))
