from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, HRFlowable, KeepTogether, Table, TableStyle

out = 'output/pdf/马世成-AIGC视频制作简历-完整版-公司名称更新.pdf'
pdfmetrics.registerFont(TTFont('MicrosoftYaHei', 'C:/Windows/Fonts/msyh.ttc'))
pdfmetrics.registerFont(TTFont('MicrosoftYaHei-Bold', 'C:/Windows/Fonts/msyhbd.ttc'))
doc = SimpleDocTemplate(out, pagesize=A4, rightMargin=17*mm, leftMargin=17*mm, topMargin=14*mm, bottomMargin=14*mm)
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='TitleCN', fontName='MicrosoftYaHei', fontSize=25, leading=30, textColor=colors.black, spaceAfter=3))
styles.add(ParagraphStyle(name='SubCN', fontName='MicrosoftYaHei', fontSize=11, leading=16, textColor=colors.black, spaceAfter=10))
styles.add(ParagraphStyle(name='HeadCN', fontName='MicrosoftYaHei-Bold', fontSize=14.5, leading=20, textColor=colors.black, borderBottomWidth=1.2, borderBottomColor=colors.black, spaceBefore=12, spaceAfter=8))
styles.add(ParagraphStyle(name='BodyCN', fontName='MicrosoftYaHei', fontSize=9.2, leading=16, textColor=colors.black, spaceAfter=6))
styles.add(ParagraphStyle(name='JobCN', fontName='MicrosoftYaHei-Bold', fontSize=11.5, leading=17, textColor=colors.black, spaceAfter=5))
header = Table([[Paragraph('姓名：马世成', styles['TitleCN']), Paragraph('电话：17361928656', styles['JobCN'])], [Paragraph('生日：2004.6.28<br/>应聘岗位：AIGC视频制作', styles['JobCN']), Paragraph('邮箱：2213467269@qq.com', styles['JobCN'])]], colWidths=[82*mm, 80*mm])
header.setStyle(TableStyle([('VALIGN',(0,0),(-1,-1),'TOP'), ('ALIGN',(1,0),(1,-1),'RIGHT'), ('LEFTPADDING',(0,0),(-1,-1),0), ('RIGHTPADDING',(0,0),(-1,-1),0), ('BOTTOMPADDING',(0,0),(-1,-1),3)]))
story = [header]
def head(x): return KeepTogether([Paragraph(x, styles['HeadCN']), HRFlowable(width='100%', thickness=1.2, color=colors.black, spaceBefore=0, spaceAfter=4)])
def body(x): return Paragraph(x, styles['BodyCN'])
def jobrow(name, date):
    row = Table([[Paragraph(name, styles['JobCN']), Paragraph(date, styles['BodyCN'])]], colWidths=[118*mm, 44*mm])
    row.setStyle(TableStyle([('VALIGN',(0,0),(-1,-1),'TOP'), ('ALIGN',(1,0),(1,0),'RIGHT'), ('LEFTPADDING',(0,0),(-1,-1),0), ('RIGHTPADDING',(0,0),(-1,-1),0)]))
    return row
def studyrow(school, major, date):
    row = Table([[Paragraph(school, styles['JobCN']), Paragraph(major, styles['JobCN']), Paragraph(date, styles['JobCN'])]], colWidths=[70*mm, 55*mm, 37*mm])
    row.setStyle(TableStyle([('VALIGN',(0,0),(-1,-1),'TOP'), ('ALIGN',(2,0),(2,0),'RIGHT'), ('LEFTPADDING',(0,0),(-1,-1),0), ('RIGHTPADDING',(0,0),(-1,-1),0)]))
    return row
story += [head('教育与学习经历'), studyrow('镇江高等职业技术学校', '数字媒体应用专业｜大专', '2022.9-2027'), body('<b>荣誉与获奖：</b>镇江市“数字影音后期制作技术”技能大赛二等奖；连续4次获得班级“技能之星”；校科艺节视频制作三等奖。'), body('学习影视特效及数字媒体制作，2026年转向学习AIGC视频制作。')]
story += [head('实习经历'), jobrow('点云文化有限公司｜AIGC视频制作', '2026.4—2026.8'), body('参与项目：《布袋和尚》《幸存者笔记》《重生80》'), body('参与AI视频素材创作；使用AI工具生成角色、道具、场景等视觉资产；负责提示词编写、画面生成、效果优化及素材迭代；配合团队完成视频创作与后期制作。')]
story += [head('擅长技能'), body('擅长使用Image2、Nano Banana、Seedream、Midjourney等AI生图工具，生成角色资产、道具资产和场景资产。'), body('使用Seedance、MiniMax、H3、Google Veo等AI视频生成工具制作动态视频素材。'), body('熟练使用即梦、LibTV、Lovart、RunningHub等画布工具进行创作。'), body('能够分析剧本，生成完整分镜本、分镜图及对应的画面提示词。'), body('熟悉ChatGPT、Gemini、DeepSeek、豆包等大语言模型，能够应用于创意构思、内容整理和提示词优化。'), body('熟悉Codex、Google AI Studio、Claude Code等Agent工具，能够辅助开发及工作流程搭建，并具备Skill技能的创建与使用经验。'), body('熟悉Photoshop、Premiere Pro、剪映、Blender、Houdini、Maya等软件。'), body('具备良好的AI提示词编写与优化能力。')]
story += [head('个人评价'), body('具备数字媒体应用专业背景及影视特效项目经验，参与过多部动漫和AI视频项目制作。工作认真负责，善于沟通协作，具备良好的团队合作能力和团队精神；学习能力、适应能力和抗压能力较强，能够积极面对工作压力并快速融入新环境。')]
doc.build(story)
