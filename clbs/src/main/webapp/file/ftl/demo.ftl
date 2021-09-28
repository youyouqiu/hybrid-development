<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<?mso-application progid="Word.Document"?>
<pkg:package xmlns:pkg="http://schemas.microsoft.com/office/2006/xmlPackage">
	<pkg:part pkg:contentType="application/vnd.openxmlformats-package.relationships+xml" pkg:name="/_rels/.rels" pkg:padding="512">
		<pkg:xmlData>
			<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
				<Relationship Id="rId3" Target="docProps/app.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties"/>
				<Relationship Id="rId2" Target="docProps/core.xml" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties"/>
				<Relationship Id="rId1" Target="word/document.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"/>
				<Relationship Id="rId4" Target="docProps/custom.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties"/>
			</Relationships>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-package.relationships+xml" pkg:name="/word/_rels/document.xml.rels" pkg:padding="256">
		<pkg:xmlData>
			<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
				<Relationship Id="rId3" Target="settings.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings"/>
				<Relationship Id="rId7" Target="theme/theme1.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme"/>
				<Relationship Id="rId2" Target="styles.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"/>
				<Relationship Id="rId1" Target="../customXml/item1.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml"/>
				<Relationship Id="rId6" Target="fontTable.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable"/>
			

				<!--生成司机图片的索引或者书签-->
				<#if (rp.drivers??&&(rp.drivers?size>0))>
					<#list rp.drivers as driverImg>				
				<Relationship Id="picId${driverImg_index}" Target="media/image${driverImg_index}.png" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"/>
					</#list>
				</#if>
				<Relationship Id="rId4" Target="webSettings.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/webSettings"/>
			</Relationships>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml" pkg:name="/word/document.xml">
		<pkg:xmlData>
			<w:document xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:ve="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing">
				<w:body>
					<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
						<w:pPr>
							<w:rPr>
								<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
							</w:rPr>
						</w:pPr>
						<w:r>
							<w:rPr>
								<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
							</w:rPr>
							<w:t>预警信息：</w:t>
						</w:r>
					</w:p>
					<w:tbl>
						<w:tblPr>
							<w:tblStyle w:val="a5"/>
							<w:tblW w:type="dxa" w:w="8522"/>
							<w:tblLayout w:type="fixed"/>
							<w:tblLook w:val="04A0"/>
						</w:tblPr>
						<w:tblGrid>
							<w:gridCol w:w="1242"/>
							<w:gridCol w:w="1598"/>
							<w:gridCol w:w="1237"/>
							<w:gridCol w:w="1603"/>
							<w:gridCol w:w="1232"/>
							<w:gridCol w:w="1610"/>
						</w:tblGrid>
						<w:tr w:rsidR="001A2636">
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>风险编号</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1598"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00E76235">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="00E76235">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.riskNumber!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1237"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>预警时间</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1603"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="009537AB">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="009537AB">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.warningTime?string['HH:mm:ss']!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1232"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>监控对象</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1610"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00AA05D6">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="00AA05D6">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.brand!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>
						<w:tr w:rsidR="001A2636">
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>风险类型</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1598"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="001F03DA">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="001F03DA">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.riskType!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1237"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>风险等级</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1603"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="005142FD">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="005142FD">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.riskLevel!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1232"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>行驶速度</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1610"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00AB7D10">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="00AB7D10">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.speed!}</w:t>
									</w:r>
									<w:r w:rsidR="00281E91">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>km/h</w:t>
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>
						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="173"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>预警记录</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="5"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>



								<!-- 预警记录 -->
								<w:tbl>
									<w:tblPr>
										<w:tblStyle w:val="a5"/>
										<w:tblW w:type="dxa" w:w="7274"/>
										<w:jc w:val="center"/>
										<w:tblBorders>
											<w:top w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:left w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:bottom w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:right w:color="auto" w:space="0" w:sz="0" w:val="none"/>
										</w:tblBorders>
										<w:tblLayout w:type="fixed"/>
										<w:tblLook w:val="04A0"/>
									</w:tblPr>
									<w:tblGrid>
										<w:gridCol w:w="1976"/>
										<w:gridCol w:w="1766"/>
										<w:gridCol w:w="1766"/>
										<w:gridCol w:w="1766"/>
									</w:tblGrid>
									
									<!-- 每行显示四列 -->
									<#assign rowNum = 4 >
									<!--初始化数据总共要展现的行数(一行4列)-->
									<#assign showRowNum = 0 >
									<!-- 计算要显示的行数-->
									<#if rp.reafList??>
										<#assign showRowNum = ((rp.reafList?size)/rowNum)?ceiling >
									</#if>
									<!-- 当显示的函数大于0，开始进行显示-->
									<#if (showRowNum > 0)>
										<!-- 将显示的行数从原来的1开始换成从0开始，方便集合从角标0开始遍历-->
										<#list 0..(showRowNum-1) as nn>
										<!--计算某一行显示的第一条数据的角标-->
										<#assign eventFirstIndex =nn*rowNum>		
										<!-- 计算显示的行数最后要显示的角标(因为freemarkerlist遍历是包含<=的，所以这里做了转换)-->
										<#assign eventLastIndex =eventFirstIndex + (rowNum-1)>												

										<w:tr w:rsidR="001A2636">
											<w:trPr>
												<w:trHeight w:val="289"/>
												<w:jc w:val="center"/>
											</w:trPr>
											<!-- 进行遍历集合-->	
											<#list  eventFirstIndex..eventLastIndex as ei>
												<#if rp.reafList[ei]??>	
											<w:tc>
											<w:tcPr>
												<w:tcW w:type="dxa" w:w="1976"/>
												<w:tcBorders>
													<w:tl2br w:val="nil"/>
													<w:tr2bl w:val="nil"/>
												</w:tcBorders>
												<w:vAlign w:val="center"/>
											</w:tcPr>
											<w:p w:rsidR="001A2636" w:rsidRDefault="00D36004">
												<w:pPr>
													<w:snapToGrid w:val="0"/>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
												</w:pPr>
												<w:r w:rsidRPr="00D36004">
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t>${rp.reafList[ei].eventTime?string['HH:mm:ss']!}</w:t>
												</w:r>
												<w:r w:rsidR="00281E91">
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t xml:space="preserve"/>
												</w:r>
												<w:r w:rsidR="00AC1AE0" w:rsidRPr="00AC1AE0">
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t>${rp.reafList[ei].riskEvent!}</w:t>
												</w:r>
											</w:p>
										</w:tc>		
										<#else>
										<!-- 为空的时候显示单元格对角线-->
										<!-- 单元格对角线-->
										<w:tc>
											<w:tcPr>
												<w:tcW w:type="dxa" w:w="1766"/>
												<w:tcBorders>
													<w:tl2br w:color="auto" w:space="0" w:sz="4" w:val="single"/>
												</w:tcBorders>
												<w:vAlign w:val="center"/>
											</w:tcPr>
											<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
												<w:pPr>
													<w:snapToGrid w:val="0"/>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
												</w:pPr>
											</w:p>
										</w:tc>
										<!---->	
										</#if>																						
											</#list>
									</w:tr>
									</#list>									

								<#else>
									<!-- 预警记录不存在，则显示一行4个对角线单元格-->
									<w:tr w:rsidR="001A2636">
										<w:trPr>
											<w:trHeight w:val="289"/>
											<w:jc w:val="center"/>
										</w:trPr>

										<#list 1..rowNum as m>
										<!-- 单元格对角线-->
										<w:tc>
											<w:tcPr>
												<w:tcW w:type="dxa" w:w="1766"/>
												<w:tcBorders>
													<w:tl2br w:color="auto" w:space="0" w:sz="4" w:val="single"/>
												</w:tcBorders>
												<w:vAlign w:val="center"/>
											</w:tcPr>
											<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
												<w:pPr>
													<w:snapToGrid w:val="0"/>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
												</w:pPr>
											</w:p>
										</w:tc>											
										</#list>									
									</w:tr>
									</#if>
									
								</w:tbl>




								<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
								</w:p>
							</w:tc>
						</w:tr>
						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="233"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>预警位置</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="5"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="004E76E3">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="004E76E3">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.address!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>
					</w:tbl>
					<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
						<w:pPr>
							<w:rPr>
								<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
							</w:rPr>
						</w:pPr>
						<w:r>
							<w:rPr>
								<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
							</w:rPr>
							<w:t>基本信息：</w:t>
						</w:r>
					</w:p>
					<w:tbl>
						<w:tblPr>
							<w:tblStyle w:val="a5"/>
							<w:tblW w:type="dxa" w:w="8522"/>
							<w:tblLayout w:type="fixed"/>
							<w:tblLook w:val="04A0"/>
						</w:tblPr>
						<w:tblGrid>
							<w:gridCol w:w="1242"/>
							<w:gridCol w:w="1598"/>
							<w:gridCol w:w="1237"/>
							<w:gridCol w:w="1603"/>
							<w:gridCol w:w="1232"/>
							<w:gridCol w:w="1610"/>
						</w:tblGrid>
						<w:tr w:rsidR="001A2636">
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>所属企业</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1598"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="003E4EDF">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="003E4EDF">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.groupName!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1237"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>车型种类</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1603"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00AB66A6">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="00AB66A6">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.vehicleType!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1232"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>车队管理电话</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1610"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="003F007C">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="003F007C">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.groupPhone!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>




							
							<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="278"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>司机信息</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="5"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:tbl>
									<w:tblPr>
										<w:tblStyle w:val="a5"/>
										<w:tblW w:type="dxa" w:w="7274"/>
										<w:jc w:val="center"/>
										<w:tblBorders>
											<w:top w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:left w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:bottom w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:right w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:insideV w:color="auto" w:space="0" w:sz="0" w:val="none"/>
										</w:tblBorders>
										<w:tblLayout w:type="fixed"/>
										<w:tblLook w:val="04A0"/>
									</w:tblPr>
									<w:tblGrid>
										<w:gridCol w:w="7274"/>
									</w:tblGrid>
							<!--初始化要展现列数目-->
							<#if (rp.drivers??&&(rp.drivers?size>0))>
							<#list  rp.drivers as driverl>	
									<w:tr w:rsidR="001A2636">
										<w:trPr>
											<w:trHeight w:val="287"/>
											<w:jc w:val="center"/>
										</w:trPr>
										<w:tc>
											<w:tcPr>
												<w:tcW w:type="dxa" w:w="7274"/>
												<w:tcBorders>
													<w:tl2br w:val="nil"/>
													<w:tr2bl w:val="nil"/>
												</w:tcBorders>
												<w:vAlign w:val="center"/>
											</w:tcPr>
											<w:p w:rsidP="007E1501" w:rsidR="001A2636" w:rsidRDefault="00281E91">
												<w:pPr>
													<w:snapToGrid w:val="0"/>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
												</w:pPr>
												<w:r>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t>&lt;${driverl_index+1}&gt;</w:t>
												</w:r>
												<w:r w:rsidR="00F32D3B" w:rsidRPr="00F32D3B">
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t>${driverl.name!}&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</w:t>
												</w:r>
												<w:r>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t xml:space="preserve">联系电话：</w:t>
												</w:r>
												<w:r w:rsidR="00A505FA" w:rsidRPr="00A505FA">
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t>${driverl.phone!}&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</w:t>
												</w:r>
												<w:r>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t xml:space="preserve"/>
												</w:r>
												<w:r w:rsidR="00D534C2">
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t>紧急联系人：</w:t>
												</w:r>
												<w:r w:rsidR="00D534C2" w:rsidRPr="00D534C2">
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t>${driverl.emergencyContact!}&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</w:t>
												</w:r>
												<w:r>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t xml:space="preserve">紧急联系人电话：</w:t>
												</w:r>
												<w:bookmarkStart w:id="0" w:name="_GoBack"/>
												<w:bookmarkEnd w:id="0"/>
												<w:r w:rsidR="007E1501" w:rsidRPr="007E1501">
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="13"/>
														<w:szCs w:val="13"/>
													</w:rPr>
													<w:t>${driverl.emergencyContactPhone!}</w:t>
												</w:r>
											</w:p>
										</w:tc>
									</w:tr>
										</#list>
										
								</#if>
								</w:tbl>
								<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
								</w:p>
							</w:tc>
						</w:tr>
						
						
						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="3218"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>司机照片</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="5"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:tbl>
									<w:tblPr>
										<w:tblStyle w:val="a5"/>
										<w:tblW w:type="dxa" w:w="7287"/>
										<w:jc w:val="center"/>
										<w:tblBorders>
											<w:top w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:left w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:bottom w:color="auto" w:space="0" w:sz="0" w:val="none"/>
											<w:right w:color="auto" w:space="0" w:sz="0" w:val="none"/>
										</w:tblBorders>
										<w:tblLayout w:type="fixed"/>
										<w:tblLook w:val="04A0"/>
									</w:tblPr>
									<w:tblGrid>
										<w:gridCol w:w="2577"/>
										<w:gridCol w:w="2355"/>
										<w:gridCol w:w="2355"/>
									</w:tblGrid>



									<!-- 每行显示四列-->
									<#assign picRowNum = 3 >
									<!--初始化数据总共要展现的行数(一行4列)-->
									<#assign showPicRowNum = 0 >
									<!-- 计算要显示的行数-->
									<#if rp.drivers??>
										<#assign showPicRowNum = ((rp.drivers?size)/picRowNum)?ceiling >
									</#if>
									<!-- 当显示的函数大于0，开始进行显示-->
									<#if (showPicRowNum > 0)>
										<!-- 将显示的行数从原来的1开始换成从0开始，方便集合从角标0开始遍历-->
										<#list 0..(showPicRowNum-1) as nn>
										<!--计算某一行显示的第一条数据的角标-->
										<#assign picFirstIndex = nn*picRowNum>		
										<!-- 计算显示的行数最后要显示的角标(因为freemarkerlist遍历是包含<=的，所以这里做了转换)-->
										<#assign picLastIndex = picFirstIndex + (picRowNum-1)>	
										<w:tr w:rsidR="001A2636">
										<w:trPr>
											<w:trHeight w:val="3221"/>
											<w:jc w:val="center"/>
										</w:trPr>										
										<#list  picFirstIndex..picLastIndex as pi>
												<#if (rp.drivers[pi].photograph)??>	
											<w:tc>
											<w:tcPr>
												<w:tcW w:type="dxa" w:w="2577"/>
												<w:tcBorders>
													<w:tl2br w:val="nil"/>
													<w:tr2bl w:val="nil"/>
												</w:tcBorders>
												<w:vAlign w:val="center"/>
											</w:tcPr>
											<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
												<w:pPr>
													<w:snapToGrid w:val="0"/>
													<w:jc w:val="center"/>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="16"/>
														<w:szCs w:val="16"/>
													</w:rPr>
												</w:pPr>
												<w:r>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
														<w:noProof/>
														<w:sz w:val="16"/>
														<w:szCs w:val="16"/>
													</w:rPr>
													<w:drawing>
														<wp:inline distB="0" distL="114300" distR="114300" distT="0">
															<wp:extent cx="1496695" cy="1496695"/>
															<wp:effectExtent b="0" l="0" r="0" t="0"/>
															<wp:docPr descr="pp" id="1" name="图片 1"/>
															<wp:cNvGraphicFramePr>
																<a:graphicFrameLocks noChangeAspect="1" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"/>
															</wp:cNvGraphicFramePr>
															<a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
																<a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
																	<pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
																		<pic:nvPicPr>
																			<pic:cNvPr descr="pp" id="1" name="图片 1"/>
																			<pic:cNvPicPr>
																				<a:picLocks noChangeAspect="1"/>
																			</pic:cNvPicPr>
																		</pic:nvPicPr>
																		<pic:blipFill>
																			<a:blip cstate="print" r:embed="picId${(pi)!}"/>
																			<a:stretch>
																				<a:fillRect/>
																			</a:stretch>
																		</pic:blipFill>
																		<pic:spPr>
																			<a:xfrm>
																				<a:off x="0" y="0"/>
																				<a:ext cx="1496695" cy="1496695"/>
																			</a:xfrm>
																			<a:prstGeom prst="rect">
																				<a:avLst/>
																			</a:prstGeom>
																		</pic:spPr>
																	</pic:pic>
																</a:graphicData>
															</a:graphic>
														</wp:inline>
													</w:drawing>
												</w:r>
											</w:p>
											<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
												<w:pPr>
													<w:snapToGrid w:val="0"/>
													<w:jc w:val="center"/>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:b/>
														<w:bCs/>
														<w:sz w:val="16"/>
														<w:szCs w:val="16"/>
													</w:rPr>
												</w:pPr>
												<w:r>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
														<w:sz w:val="16"/>
														<w:szCs w:val="16"/>
													</w:rPr>
													<w:t>&lt;${pi+1}&gt;</w:t>
												</w:r>
												<w:r w:rsidR="00B71F79" w:rsidRPr="00B71F79">
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="16"/>
														<w:szCs w:val="16"/>
													</w:rPr>
													<w:t>${rp.drivers[pi].name!}</w:t>
												</w:r>
											</w:p>
										</w:tc>														
												<#else>
											<w:tc>
											<w:tcPr>
												<w:tcW w:type="dxa" w:w="2355"/>
												<w:tcBorders>
													<w:tl2br w:color="auto" w:space="0" w:sz="4" w:val="single"/>
												</w:tcBorders>
												<w:vAlign w:val="center"/>
											</w:tcPr>
											<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
												<w:pPr>
													<w:snapToGrid w:val="0"/>
													<w:jc w:val="center"/>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="16"/>
														<w:szCs w:val="16"/>
													</w:rPr>
												</w:pPr>
											</w:p>
										</w:tc>													
													
												</#if>
										</#list>	
										</w:tr>												
									</#list>									

								<#else>
									<w:tr w:rsidR="001A2636">
										<w:trPr>
											<w:trHeight w:val="3221"/>
											<w:jc w:val="center"/>
										</w:trPr>	
									<!--图片不存在显示三个对角线单元格-->
										<#list 1..picRowNum as m>
										<w:tc>
											<w:tcPr>
												<w:tcW w:type="dxa" w:w="2355"/>
												<w:tcBorders>
													<w:tl2br w:color="auto" w:space="0" w:sz="4" w:val="single"/>
												</w:tcBorders>
												<w:vAlign w:val="center"/>
											</w:tcPr>
											<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
												<w:pPr>
													<w:snapToGrid w:val="0"/>
													<w:jc w:val="center"/>
													<w:rPr>
														<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
														<w:sz w:val="16"/>
														<w:szCs w:val="16"/>
													</w:rPr>
												</w:pPr>
											</w:p>
										</w:tc>
									</#list>
								</w:tr>									
										
								</#if>
								</w:tbl>
								<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
								</w:p>
							</w:tc>
						</w:tr>
					</w:tbl>
					<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
						<w:pPr>
							<w:rPr>
								<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
							</w:rPr>
						</w:pPr>
						<w:r>
							<w:rPr>
								<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
							</w:rPr>
							<w:t>风控处理：</w:t>
						</w:r>
					</w:p>
					<w:tbl>
						<w:tblPr>
							<w:tblStyle w:val="a5"/>
							<w:tblW w:type="dxa" w:w="8522"/>
							<w:tblLayout w:type="fixed"/>
							<w:tblLook w:val="04A0"/>
						</w:tblPr>
						<w:tblGrid>
							<w:gridCol w:w="1242"/>
							<w:gridCol w:w="1418"/>
							<w:gridCol w:w="5862"/>
						</w:tblGrid>
						<w:tr w:rsidR="001A2636">
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>处理人</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="2"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00287DE9">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="00287DE9">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.dealId!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>
						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="1619"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:vMerge w:val="restart"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>处理</w:t>
									</w:r>
								</w:p>
							</w:tc>
							
							<!--处理，回访表第一条记录-->
							<#if rp.dealVisit??>
							<#assign i=1>	
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="2"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<#if rp.dealVisit.warningAccuracy?? >		
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve">${i!}. </w:t>
									</w:r>
									<w:r w:rsidR="00B52BE9" w:rsidRPr="00B52BE9">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${rp.dealVisit.warningAccuracy!}；</w:t>
									</w:r>
								</w:p>
							<#assign i=i+1>
							</#if>		
								<#if rp.dealVisit.warnAfterStatus?? >	
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve">${i!}. </w:t>
									</w:r>
									<w:r w:rsidR="003859D9" w:rsidRPr="003859D9">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${rp.dealVisit.warnAfterStatus!}；</w:t>
									</w:r>
								</w:p>
									<#assign i=i+1>
									</#if>		
									<#if rp.dealVisit.interventionPersonnel?? &&rp.dealVisit.interventionPersonnel!="" >									
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve">${i!}. </w:t>
									</w:r>
									<w:r w:rsidR="00CA433B" w:rsidRPr="00CA433B">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${rp.dealVisit.interventionPersonnel!}；</w:t>
									</w:r>
								</w:p>
									<#assign i=i+1>
									</#if>		
									<#if rp.dealVisit.interventionAfterStatus?? >									
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve">${i!}. </w:t>
									</w:r>
									<w:r w:rsidR="00125385" w:rsidRPr="00125385">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${rp.dealVisit.interventionAfterStatus!}；</w:t>
									</w:r>
								</w:p>
									<#assign i=i+1>
									</#if>	
										<#if rp.dealVisit.warningLevel?? >									
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve">${i!}. </w:t>
									</w:r>
									<w:r w:rsidR="002D5B63" w:rsidRPr="002D5B63">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${rp.dealVisit.warningLevel!}；</w:t>
									</w:r>
								</w:p>
									<#assign i=i+1>
									</#if>	
									<#if rp.dealVisit.content?? &&rp.dealVisit.content?trim!="" >										
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve">${i!}. </w:t>
									</w:r>
									<w:r w:rsidR="003F76B6" w:rsidRPr="003F76B6">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${rp.dealVisit.content!}。</w:t>
									</w:r>
								</w:p>
								<!-- 防止前面都为空的情况，少了<w:p>元素-->
								</#if>
								<#if i<2>
								 <w:p ></w:p>	
								</#if>									
							</w:tc>
						<#else>
						<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="2"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidR="00B52BE9" w:rsidRPr="00B52BE9">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t></w:t>
									</w:r>
								</w:p>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidR="003859D9" w:rsidRPr="003859D9">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t></w:t>
									</w:r>
								</w:p>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidR="00CA433B" w:rsidRPr="00CA433B">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t></w:t>
									</w:r>
								</w:p>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidR="00125385" w:rsidRPr="00125385">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t></w:t>
									</w:r>
								</w:p>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidR="002D5B63" w:rsidRPr="002D5B63">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t></w:t>
									</w:r>
								</w:p>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidR="003F76B6" w:rsidRPr="003F76B6">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t></w:t>
									</w:r>
								</w:p>
							</w:tc>

						</#if>	
						</w:tr>




						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="416"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:vMerge/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1418"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>司机名称</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="5862"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t></w:t>
									</w:r>
									<w:r w:rsidR="00942152" w:rsidRPr="00942152">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<#if rp.dealVisit??&&rp.dealVisit.driverName??&&rp.dealVisit.driverName!="">
										<w:t>&lt;1&gt;${rp.dealVisit.driverName!}</w:t>
											<#else>
											<w:t></w:t>
										</#if>
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>
						<!--回访表-->
						<#if rp.riskVisits??>
						<#list rp.riskVisits as riskVisit>
						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="1547"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:vMerge w:val="restart"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>回访${riskVisit_index+1}</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="2"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<#assign x=1>
								<#if riskVisit.warningAccuracy??>										
								<w:p w:rsidP="000943ED" w:rsidR="000943ED" w:rsidRDefault="000943ED">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidRPr="00B52BE9">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${x}. ${riskVisit.warningAccuracy!}；</w:t>
									</w:r>
								</w:p>
							<#assign x=x+1>
							</#if>		
							<#if riskVisit.warnAfterStatus?? >	
								<w:p w:rsidP="000943ED" w:rsidR="000943ED" w:rsidRDefault="000943ED">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidRPr="003859D9">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${x}. ${riskVisit.warnAfterStatus!}；</w:t>
									</w:r>
								</w:p>
							<#assign x=x+1>
							</#if>		
							<#if riskVisit.interventionPersonnel??>										
								<w:p w:rsidP="000943ED" w:rsidR="000943ED" w:rsidRDefault="000943ED">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidRPr="00CA433B">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${x}. ${riskVisit.interventionPersonnel!}；</w:t>
									</w:r>
								</w:p>
							<#assign x=x+1>
							</#if>	
							<#if riskVisit.interventionAfterStatus??>								
								<w:p w:rsidP="000943ED" w:rsidR="000943ED" w:rsidRDefault="000943ED">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidRPr="00125385">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${x}. ${riskVisit.interventionAfterStatus!}；</w:t>
									</w:r>
								</w:p>
							<#assign x=x+1>
							</#if>	
							<#if riskVisit.warningLevel?? >										
								<w:p w:rsidP="000943ED" w:rsidR="000943ED" w:rsidRDefault="000943ED">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidRPr="002D5B63">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${x}. ${riskVisit.warningLevel!}；</w:t>
									</w:r>
								</w:p>
								<#assign x=x+1>
							</#if>		
							<#if riskVisit.content?? && riskVisit.content!="">										
								<w:p w:rsidP="000943ED" w:rsidR="001A2636" w:rsidRDefault="000943ED">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t xml:space="preserve"></w:t>
									</w:r>
									<w:r w:rsidRPr="003F76B6">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>${x}. ${riskVisit.content!}。</w:t>
									</w:r>
								</w:p>

							</#if>		
								<!--处理上面都为空的时候，<w:tc>依赖<w:p》的问题-->
								<#if x<2>
								 <w:p ></w:p>	
								</#if>											
							</w:tc>
						</w:tr>
						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="416"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:vMerge/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1418"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t>司机名称</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="5862"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<w:t></w:t>
									</w:r>
									<w:r w:rsidR="00942152" w:rsidRPr="00942152">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="13"/>
											<w:szCs w:val="13"/>
										</w:rPr>
										<#if (riskVisit.driverName??&&riskVisit.driverName!="")>
												<w:t>&lt;1&gt;${riskVisit.driverName!}</w:t>
										<#else>
											<w:t></w:t>
										</#if>
										
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>
						</#list>
					</#if>


						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="334"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>回访理由1</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="2"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00DB716E">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="00DB716E">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<!-- 回访理由-->
										<#if rp.dealVisit??>
										<w:t>${rp.dealVisit.reason!}</w:t>
										<#else>
											<w:t></w:t>
										</#if>
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>
						
						<#if rp.reafList??>
						<#list rp.riskVisits as riskVisit>							
						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="334"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>回访理由${riskVisit_index+2}</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="2"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="004C71DF">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="00DB716E">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${riskVisit.reason!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>
						</#list>
					</#if>

						<w:tr w:rsidR="001A2636">
							<w:trPr>
								<w:trHeight w:val="334"/>
							</w:trPr>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="1242"/>
									<w:shd w:color="auto" w:fill="DEF6FF" w:val="clear"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="center"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑" w:hint="eastAsia"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>风控结果</w:t>
									</w:r>
								</w:p>
							</w:tc>
							<w:tc>
								<w:tcPr>
									<w:tcW w:type="dxa" w:w="7280"/>
									<w:gridSpan w:val="2"/>
									<w:vAlign w:val="center"/>
								</w:tcPr>
								<w:p w:rsidR="001A2636" w:rsidRDefault="00281E91">
									<w:pPr>
										<w:snapToGrid w:val="0"/>
										<w:jc w:val="left"/>
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
									</w:pPr>
									<w:r w:rsidRPr="00281E91">
										<w:rPr>
											<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
											<w:sz w:val="16"/>
											<w:szCs w:val="16"/>
										</w:rPr>
										<w:t>${rp.riskResult!}</w:t>
									</w:r>
								</w:p>
							</w:tc>
						</w:tr>
					</w:tbl>
					<w:p w:rsidR="001A2636" w:rsidRDefault="001A2636">
						<w:pPr>
							<w:rPr>
								<w:rFonts w:ascii="微软雅黑" w:eastAsia="微软雅黑" w:hAnsi="微软雅黑"/>
							</w:rPr>
						</w:pPr>
					</w:p>
					<w:sectPr w:rsidR="001A2636" w:rsidSect="001A2636">
						<w:pgSz w:h="16838" w:w="11906"/>
						<w:pgMar w:bottom="1440" w:footer="992" w:gutter="0" w:header="851" w:left="1800" w:right="1800" w:top="1440"/>
						<w:cols w:space="425"/>
						<w:docGrid w:linePitch="312" w:type="lines"/>
					</w:sectPr>
				</w:body>
			</w:document>
		</pkg:xmlData>
	</pkg:part>

	<!--生成图片的base64编码-->
	<#if (rp.drivers??&&(rp.drivers?size>0))>
		<#list rp.drivers as driverImg>
<pkg:part pkg:compression="store" pkg:contentType="image/png" pkg:name="/word/media/image${driverImg_index}.png">
		<pkg:binaryData>${rp.drivers[driverImg_index].photograph!}</pkg:binaryData>
</pkg:part>	
		</#list>
	</#if>

	<pkg:part pkg:contentType="application/vnd.openxmlformats-officedocument.theme+xml" pkg:name="/word/theme/theme1.xml">
		<pkg:xmlData>
			<a:theme name="Office 主题" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
				<a:themeElements>
					<a:clrScheme name="Office">
						<a:dk1>
							<a:sysClr lastClr="000000" val="windowText"/>
						</a:dk1>
						<a:lt1>
							<a:sysClr lastClr="FFFFFF" val="window"/>
						</a:lt1>
						<a:dk2>
							<a:srgbClr val="1F497D"/>
						</a:dk2>
						<a:lt2>
							<a:srgbClr val="EEECE1"/>
						</a:lt2>
						<a:accent1>
							<a:srgbClr val="4F81BD"/>
						</a:accent1>
						<a:accent2>
							<a:srgbClr val="C0504D"/>
						</a:accent2>
						<a:accent3>
							<a:srgbClr val="9BBB59"/>
						</a:accent3>
						<a:accent4>
							<a:srgbClr val="8064A2"/>
						</a:accent4>
						<a:accent5>
							<a:srgbClr val="4BACC6"/>
						</a:accent5>
						<a:accent6>
							<a:srgbClr val="F79646"/>
						</a:accent6>
						<a:hlink>
							<a:srgbClr val="0000FF"/>
						</a:hlink>
						<a:folHlink>
							<a:srgbClr val="800080"/>
						</a:folHlink>
					</a:clrScheme>
					<a:fontScheme name="Office">
						<a:majorFont>
							<a:latin typeface="Cambria"/>
							<a:ea typeface=""/>
							<a:cs typeface=""/>
							<a:font script="Jpan" typeface="ＭＳ ゴシック"/>
							<a:font script="Hang" typeface="맑은 고딕"/>
							<a:font script="Hans" typeface="宋体"/>
							<a:font script="Hant" typeface="新細明體"/>
							<a:font script="Arab" typeface="Times New Roman"/>
							<a:font script="Hebr" typeface="Times New Roman"/>
							<a:font script="Thai" typeface="Angsana New"/>
							<a:font script="Ethi" typeface="Nyala"/>
							<a:font script="Beng" typeface="Vrinda"/>
							<a:font script="Gujr" typeface="Shruti"/>
							<a:font script="Khmr" typeface="MoolBoran"/>
							<a:font script="Knda" typeface="Tunga"/>
							<a:font script="Guru" typeface="Raavi"/>
							<a:font script="Cans" typeface="Euphemia"/>
							<a:font script="Cher" typeface="Plantagenet Cherokee"/>
							<a:font script="Yiii" typeface="Microsoft Yi Baiti"/>
							<a:font script="Tibt" typeface="Microsoft Himalaya"/>
							<a:font script="Thaa" typeface="MV Boli"/>
							<a:font script="Deva" typeface="Mangal"/>
							<a:font script="Telu" typeface="Gautami"/>
							<a:font script="Taml" typeface="Latha"/>
							<a:font script="Syrc" typeface="Estrangelo Edessa"/>
							<a:font script="Orya" typeface="Kalinga"/>
							<a:font script="Mlym" typeface="Kartika"/>
							<a:font script="Laoo" typeface="DokChampa"/>
							<a:font script="Sinh" typeface="Iskoola Pota"/>
							<a:font script="Mong" typeface="Mongolian Baiti"/>
							<a:font script="Viet" typeface="Times New Roman"/>
							<a:font script="Uigh" typeface="Microsoft Uighur"/>
						</a:majorFont>
						<a:minorFont>
							<a:latin typeface="Calibri"/>
							<a:ea typeface=""/>
							<a:cs typeface=""/>
							<a:font script="Jpan" typeface="ＭＳ 明朝"/>
							<a:font script="Hang" typeface="맑은 고딕"/>
							<a:font script="Hans" typeface="宋体"/>
							<a:font script="Hant" typeface="新細明體"/>
							<a:font script="Arab" typeface="Arial"/>
							<a:font script="Hebr" typeface="Arial"/>
							<a:font script="Thai" typeface="Cordia New"/>
							<a:font script="Ethi" typeface="Nyala"/>
							<a:font script="Beng" typeface="Vrinda"/>
							<a:font script="Gujr" typeface="Shruti"/>
							<a:font script="Khmr" typeface="DaunPenh"/>
							<a:font script="Knda" typeface="Tunga"/>
							<a:font script="Guru" typeface="Raavi"/>
							<a:font script="Cans" typeface="Euphemia"/>
							<a:font script="Cher" typeface="Plantagenet Cherokee"/>
							<a:font script="Yiii" typeface="Microsoft Yi Baiti"/>
							<a:font script="Tibt" typeface="Microsoft Himalaya"/>
							<a:font script="Thaa" typeface="MV Boli"/>
							<a:font script="Deva" typeface="Mangal"/>
							<a:font script="Telu" typeface="Gautami"/>
							<a:font script="Taml" typeface="Latha"/>
							<a:font script="Syrc" typeface="Estrangelo Edessa"/>
							<a:font script="Orya" typeface="Kalinga"/>
							<a:font script="Mlym" typeface="Kartika"/>
							<a:font script="Laoo" typeface="DokChampa"/>
							<a:font script="Sinh" typeface="Iskoola Pota"/>
							<a:font script="Mong" typeface="Mongolian Baiti"/>
							<a:font script="Viet" typeface="Arial"/>
							<a:font script="Uigh" typeface="Microsoft Uighur"/>
						</a:minorFont>
					</a:fontScheme>
					<a:fmtScheme name="Office">
						<a:fillStyleLst>
							<a:solidFill>
								<a:schemeClr val="phClr"/>
							</a:solidFill>
							<a:gradFill rotWithShape="1">
								<a:gsLst>
									<a:gs pos="0">
										<a:schemeClr val="phClr">
											<a:tint val="50000"/>
											<a:satMod val="300000"/>
										</a:schemeClr>
									</a:gs>
									<a:gs pos="35000">
										<a:schemeClr val="phClr">
											<a:tint val="37000"/>
											<a:satMod val="300000"/>
										</a:schemeClr>
									</a:gs>
									<a:gs pos="100000">
										<a:schemeClr val="phClr">
											<a:tint val="15000"/>
											<a:satMod val="350000"/>
										</a:schemeClr>
									</a:gs>
								</a:gsLst>
								<a:lin ang="16200000" scaled="1"/>
							</a:gradFill>
							<a:gradFill rotWithShape="1">
								<a:gsLst>
									<a:gs pos="0">
										<a:schemeClr val="phClr">
											<a:shade val="51000"/>
											<a:satMod val="130000"/>
										</a:schemeClr>
									</a:gs>
									<a:gs pos="80000">
										<a:schemeClr val="phClr">
											<a:shade val="93000"/>
											<a:satMod val="130000"/>
										</a:schemeClr>
									</a:gs>
									<a:gs pos="100000">
										<a:schemeClr val="phClr">
											<a:shade val="94000"/>
											<a:satMod val="135000"/>
										</a:schemeClr>
									</a:gs>
								</a:gsLst>
								<a:lin ang="16200000" scaled="0"/>
							</a:gradFill>
						</a:fillStyleLst>
						<a:lnStyleLst>
							<a:ln algn="ctr" cap="flat" cmpd="sng" w="9525">
								<a:solidFill>
									<a:schemeClr val="phClr">
										<a:shade val="95000"/>
										<a:satMod val="105000"/>
									</a:schemeClr>
								</a:solidFill>
								<a:prstDash val="solid"/>
							</a:ln>
							<a:ln algn="ctr" cap="flat" cmpd="sng" w="25400">
								<a:solidFill>
									<a:schemeClr val="phClr"/>
								</a:solidFill>
								<a:prstDash val="solid"/>
							</a:ln>
							<a:ln algn="ctr" cap="flat" cmpd="sng" w="38100">
								<a:solidFill>
									<a:schemeClr val="phClr"/>
								</a:solidFill>
								<a:prstDash val="solid"/>
							</a:ln>
						</a:lnStyleLst>
						<a:effectStyleLst>
							<a:effectStyle>
								<a:effectLst>
									<a:outerShdw blurRad="40000" dir="5400000" dist="20000" rotWithShape="0">
										<a:srgbClr val="000000">
											<a:alpha val="38000"/>
										</a:srgbClr>
									</a:outerShdw>
								</a:effectLst>
							</a:effectStyle>
							<a:effectStyle>
								<a:effectLst>
									<a:outerShdw blurRad="40000" dir="5400000" dist="23000" rotWithShape="0">
										<a:srgbClr val="000000">
											<a:alpha val="35000"/>
										</a:srgbClr>
									</a:outerShdw>
								</a:effectLst>
							</a:effectStyle>
							<a:effectStyle>
								<a:effectLst>
									<a:outerShdw blurRad="40000" dir="5400000" dist="23000" rotWithShape="0">
										<a:srgbClr val="000000">
											<a:alpha val="35000"/>
										</a:srgbClr>
									</a:outerShdw>
								</a:effectLst>
								<a:scene3d>
									<a:camera prst="orthographicFront">
										<a:rot lat="0" lon="0" rev="0"/>
									</a:camera>
									<a:lightRig dir="t" rig="threePt">
										<a:rot lat="0" lon="0" rev="1200000"/>
									</a:lightRig>
								</a:scene3d>
								<a:sp3d>
									<a:bevelT h="25400" w="63500"/>
								</a:sp3d>
							</a:effectStyle>
						</a:effectStyleLst>
						<a:bgFillStyleLst>
							<a:solidFill>
								<a:schemeClr val="phClr"/>
							</a:solidFill>
							<a:gradFill rotWithShape="1">
								<a:gsLst>
									<a:gs pos="0">
										<a:schemeClr val="phClr">
											<a:tint val="40000"/>
											<a:satMod val="350000"/>
										</a:schemeClr>
									</a:gs>
									<a:gs pos="40000">
										<a:schemeClr val="phClr">
											<a:tint val="45000"/>
											<a:shade val="99000"/>
											<a:satMod val="350000"/>
										</a:schemeClr>
									</a:gs>
									<a:gs pos="100000">
										<a:schemeClr val="phClr">
											<a:shade val="20000"/>
											<a:satMod val="255000"/>
										</a:schemeClr>
									</a:gs>
								</a:gsLst>
								<a:path path="circle">
									<a:fillToRect b="180000" l="50000" r="50000" t="-80000"/>
								</a:path>
							</a:gradFill>
							<a:gradFill rotWithShape="1">
								<a:gsLst>
									<a:gs pos="0">
										<a:schemeClr val="phClr">
											<a:tint val="80000"/>
											<a:satMod val="300000"/>
										</a:schemeClr>
									</a:gs>
									<a:gs pos="100000">
										<a:schemeClr val="phClr">
											<a:shade val="30000"/>
											<a:satMod val="200000"/>
										</a:schemeClr>
									</a:gs>
								</a:gsLst>
								<a:path path="circle">
									<a:fillToRect b="50000" l="50000" r="50000" t="50000"/>
								</a:path>
							</a:gradFill>
						</a:bgFillStyleLst>
					</a:fmtScheme>
				</a:themeElements>
				<a:objectDefaults/>
				<a:extraClrSchemeLst/>
			</a:theme>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml" pkg:name="/word/settings.xml">
		<pkg:xmlData>
			<w:settings xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:sl="http://schemas.openxmlformats.org/schemaLibrary/2006/main" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:w10="urn:schemas-microsoft-com:office:word">
				<w:zoom w:percent="172"/>
				<w:bordersDoNotSurroundHeader/>
				<w:bordersDoNotSurroundFooter/>
				<w:defaultTabStop w:val="420"/>
				<w:drawingGridVerticalSpacing w:val="156"/>
				<w:displayHorizontalDrawingGridEvery w:val="0"/>
				<w:displayVerticalDrawingGridEvery w:val="2"/>
				<w:characterSpacingControl w:val="compressPunctuation"/>
				<w:compat>
					<w:spaceForUL/>
					<w:balanceSingleByteDoubleByteWidth/>
					<w:doNotLeaveBackslashAlone/>
					<w:ulTrailSpace/>
					<w:doNotExpandShiftReturn/>
					<w:adjustLineHeightInTable/>
					<w:useFELayout/>
				</w:compat>
				<w:rsids>
					<w:rsidRoot w:val="009D0867"/>
					<w:rsid w:val="00017DE8"/>
					<w:rsid w:val="00036965"/>
					<w:rsid w:val="000678D4"/>
					<w:rsid w:val="000943ED"/>
					<w:rsid w:val="000A1947"/>
					<w:rsid w:val="00125385"/>
					<w:rsid w:val="001A2636"/>
					<w:rsid w:val="001E71EF"/>
					<w:rsid w:val="001F03DA"/>
					<w:rsid w:val="002511D6"/>
					<w:rsid w:val="00255A11"/>
					<w:rsid w:val="00281E91"/>
					<w:rsid w:val="00287DE9"/>
					<w:rsid w:val="002D55DE"/>
					<w:rsid w:val="002D5B63"/>
					<w:rsid w:val="002F4DF3"/>
					<w:rsid w:val="00362CFF"/>
					<w:rsid w:val="003859D9"/>
					<w:rsid w:val="003E4EDF"/>
					<w:rsid w:val="003F007C"/>
					<w:rsid w:val="003F76B6"/>
					<w:rsid w:val="004A1299"/>
					<w:rsid w:val="004C71DF"/>
					<w:rsid w:val="004D75F1"/>
					<w:rsid w:val="004E76E3"/>
					<w:rsid w:val="005142FD"/>
					<w:rsid w:val="005F2B8D"/>
					<w:rsid w:val="006702AE"/>
					<w:rsid w:val="007E1501"/>
					<w:rsid w:val="00942152"/>
					<w:rsid w:val="009537AB"/>
					<w:rsid w:val="009D0867"/>
					<w:rsid w:val="009F3853"/>
					<w:rsid w:val="00A32578"/>
					<w:rsid w:val="00A505FA"/>
					<w:rsid w:val="00AA05D6"/>
					<w:rsid w:val="00AB66A6"/>
					<w:rsid w:val="00AB7D10"/>
					<w:rsid w:val="00AC1AE0"/>
					<w:rsid w:val="00B0662A"/>
					<w:rsid w:val="00B52BE9"/>
					<w:rsid w:val="00B71F79"/>
					<w:rsid w:val="00C72FDC"/>
					<w:rsid w:val="00CA433B"/>
					<w:rsid w:val="00D14224"/>
					<w:rsid w:val="00D36004"/>
					<w:rsid w:val="00D534C2"/>
					<w:rsid w:val="00DA7662"/>
					<w:rsid w:val="00DB716E"/>
					<w:rsid w:val="00E1711F"/>
					<w:rsid w:val="00E66E66"/>
					<w:rsid w:val="00E67592"/>
					<w:rsid w:val="00E76235"/>
					<w:rsid w:val="00EE0262"/>
					<w:rsid w:val="00F32D3B"/>
					<w:rsid w:val="00F80CF9"/>
					<w:rsid w:val="00FA5D49"/>
					<w:rsid w:val="00FD541D"/>
					<w:rsid w:val="01701246"/>
					<w:rsid w:val="03393014"/>
					<w:rsid w:val="05505B24"/>
					<w:rsid w:val="0A7C4606"/>
					<w:rsid w:val="0AB3785F"/>
					<w:rsid w:val="1053707F"/>
					<w:rsid w:val="11B95212"/>
					<w:rsid w:val="124E36D3"/>
					<w:rsid w:val="14CD7C8F"/>
					<w:rsid w:val="154E7D51"/>
					<w:rsid w:val="1585514C"/>
					<w:rsid w:val="1F96503B"/>
					<w:rsid w:val="1FA0004A"/>
					<w:rsid w:val="20543445"/>
					<w:rsid w:val="226351E3"/>
					<w:rsid w:val="22C6103B"/>
					<w:rsid w:val="24B860B8"/>
					<w:rsid w:val="2980158C"/>
					<w:rsid w:val="2C696537"/>
					<w:rsid w:val="315162CF"/>
					<w:rsid w:val="31D064E5"/>
					<w:rsid w:val="34DA7EC9"/>
					<w:rsid w:val="35254044"/>
					<w:rsid w:val="36395578"/>
					<w:rsid w:val="38056FCB"/>
					<w:rsid w:val="399D723D"/>
					<w:rsid w:val="3EA121DB"/>
					<w:rsid w:val="411F0C05"/>
					<w:rsid w:val="44E87D9A"/>
					<w:rsid w:val="45001654"/>
					<w:rsid w:val="471422FE"/>
					<w:rsid w:val="481E7970"/>
					<w:rsid w:val="48B35C1E"/>
					<w:rsid w:val="49F572BE"/>
					<w:rsid w:val="4E9A3F86"/>
					<w:rsid w:val="510F0FD2"/>
					<w:rsid w:val="52401649"/>
					<w:rsid w:val="52F21CA4"/>
					<w:rsid w:val="57A67C07"/>
					<w:rsid w:val="58E35B5C"/>
					<w:rsid w:val="5C9008E9"/>
					<w:rsid w:val="617D123B"/>
					<w:rsid w:val="61EF1267"/>
					<w:rsid w:val="657E56CE"/>
					<w:rsid w:val="65C30AC7"/>
					<w:rsid w:val="65F3397E"/>
					<w:rsid w:val="66406E9E"/>
					<w:rsid w:val="67460CC1"/>
					<w:rsid w:val="683E1C75"/>
					<w:rsid w:val="6B2C5167"/>
					<w:rsid w:val="6CB755A2"/>
					<w:rsid w:val="6F41607D"/>
					<w:rsid w:val="6F4F1543"/>
					<w:rsid w:val="76BA12FB"/>
					<w:rsid w:val="77597113"/>
					<w:rsid w:val="7A1B64F5"/>
					<w:rsid w:val="7B420B74"/>
					<w:rsid w:val="7B7548B1"/>
					<w:rsid w:val="7D580E49"/>
					<w:rsid w:val="7E24788C"/>
					<w:rsid w:val="7F651358"/>
				</w:rsids>
				<m:mathPr>
					<m:mathFont m:val="Cambria Math"/>
					<m:brkBin m:val="before"/>
					<m:brkBinSub m:val="--"/>
					<m:smallFrac m:val="off"/>
					<m:dispDef/>
					<m:lMargin m:val="0"/>
					<m:rMargin m:val="0"/>
					<m:defJc m:val="centerGroup"/>
					<m:wrapIndent m:val="1440"/>
					<m:intLim m:val="subSup"/>
					<m:naryLim m:val="undOvr"/>
				</m:mathPr>
				<w:themeFontLang w:eastAsia="zh-CN" w:val="en-US"/>
				<w:clrSchemeMapping w:accent1="accent1" w:accent2="accent2" w:accent3="accent3" w:accent4="accent4" w:accent5="accent5" w:accent6="accent6" w:bg1="light1" w:bg2="light2" w:followedHyperlink="followedHyperlink" w:hyperlink="hyperlink" w:t1="dark1" w:t2="dark2"/>
				<w:shapeDefaults>
					<o:shapedefaults spidmax="3074" v:ext="edit"/>
					<o:shapelayout v:ext="edit">
						<o:idmap data="1" v:ext="edit"/>
					</o:shapelayout>
				</w:shapeDefaults>
				<w:decimalSymbol w:val="."/>
				<w:listSeparator w:val=","/>
			</w:settings>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml" pkg:name="/word/styles.xml">
		<pkg:xmlData>
			<w:styles xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
				<w:docDefaults>
					<w:rPrDefault>
						<w:rPr>
							<w:rFonts w:asciiTheme="minorHAnsi" w:cstheme="minorBidi" w:eastAsiaTheme="minorEastAsia" w:hAnsiTheme="minorHAnsi"/>
							<w:lang w:bidi="ar-SA" w:eastAsia="zh-CN" w:val="en-US"/>
						</w:rPr>
					</w:rPrDefault>
					<w:pPrDefault/>
				</w:docDefaults>
				<w:latentStyles w:count="267" w:defLockedState="0" w:defQFormat="0" w:defSemiHidden="1" w:defUIPriority="99" w:defUnhideWhenUsed="1">
					<w:lsdException w:name="Normal" w:qFormat="1" w:semiHidden="0" w:uiPriority="0" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="heading 1" w:qFormat="1" w:semiHidden="0" w:uiPriority="9" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="heading 2" w:qFormat="1" w:uiPriority="9"/>
					<w:lsdException w:name="heading 3" w:qFormat="1" w:uiPriority="9"/>
					<w:lsdException w:name="heading 4" w:qFormat="1" w:uiPriority="9"/>
					<w:lsdException w:name="heading 5" w:qFormat="1" w:uiPriority="9"/>
					<w:lsdException w:name="heading 6" w:qFormat="1" w:uiPriority="9"/>
					<w:lsdException w:name="heading 7" w:qFormat="1" w:uiPriority="9"/>
					<w:lsdException w:name="heading 8" w:qFormat="1" w:uiPriority="9"/>
					<w:lsdException w:name="heading 9" w:qFormat="1" w:uiPriority="9"/>
					<w:lsdException w:name="toc 1" w:uiPriority="39"/>
					<w:lsdException w:name="toc 2" w:uiPriority="39"/>
					<w:lsdException w:name="toc 3" w:uiPriority="39"/>
					<w:lsdException w:name="toc 4" w:uiPriority="39"/>
					<w:lsdException w:name="toc 5" w:uiPriority="39"/>
					<w:lsdException w:name="toc 6" w:uiPriority="39"/>
					<w:lsdException w:name="toc 7" w:uiPriority="39"/>
					<w:lsdException w:name="toc 8" w:uiPriority="39"/>
					<w:lsdException w:name="toc 9" w:uiPriority="39"/>
					<w:lsdException w:name="header" w:semiHidden="0"/>
					<w:lsdException w:name="footer" w:semiHidden="0"/>
					<w:lsdException w:name="caption" w:qFormat="1" w:uiPriority="35"/>
					<w:lsdException w:name="Title" w:qFormat="1" w:semiHidden="0" w:uiPriority="10" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Default Paragraph Font" w:uiPriority="1"/>
					<w:lsdException w:name="Subtitle" w:qFormat="1" w:semiHidden="0" w:uiPriority="11" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Strong" w:qFormat="1" w:semiHidden="0" w:uiPriority="22" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Emphasis" w:qFormat="1" w:semiHidden="0" w:uiPriority="20" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Normal Table" w:qFormat="1"/>
					<w:lsdException w:name="Table Grid" w:semiHidden="0" w:uiPriority="59" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Shading" w:semiHidden="0" w:uiPriority="60" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light List" w:semiHidden="0" w:uiPriority="61" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Grid" w:semiHidden="0" w:uiPriority="62" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 1" w:semiHidden="0" w:uiPriority="63" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 2" w:semiHidden="0" w:uiPriority="64" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 1" w:semiHidden="0" w:uiPriority="65" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 2" w:semiHidden="0" w:uiPriority="66" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 1" w:semiHidden="0" w:uiPriority="67" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 2" w:semiHidden="0" w:uiPriority="68" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 3" w:semiHidden="0" w:uiPriority="69" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Dark List" w:semiHidden="0" w:uiPriority="70" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Shading" w:semiHidden="0" w:uiPriority="71" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful List" w:semiHidden="0" w:uiPriority="72" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Grid" w:semiHidden="0" w:uiPriority="73" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Shading Accent 1" w:semiHidden="0" w:uiPriority="60" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light List Accent 1" w:semiHidden="0" w:uiPriority="61" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Grid Accent 1" w:semiHidden="0" w:uiPriority="62" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 1 Accent 1" w:semiHidden="0" w:uiPriority="63" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 2 Accent 1" w:semiHidden="0" w:uiPriority="64" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 1 Accent 1" w:semiHidden="0" w:uiPriority="65" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 2 Accent 1" w:semiHidden="0" w:uiPriority="66" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 1 Accent 1" w:semiHidden="0" w:uiPriority="67" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 2 Accent 1" w:semiHidden="0" w:uiPriority="68" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 3 Accent 1" w:semiHidden="0" w:uiPriority="69" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Dark List Accent 1" w:semiHidden="0" w:uiPriority="70" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Shading Accent 1" w:semiHidden="0" w:uiPriority="71" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful List Accent 1" w:semiHidden="0" w:uiPriority="72" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Grid Accent 1" w:semiHidden="0" w:uiPriority="73" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Shading Accent 2" w:semiHidden="0" w:uiPriority="60" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light List Accent 2" w:semiHidden="0" w:uiPriority="61" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Grid Accent 2" w:semiHidden="0" w:uiPriority="62" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 1 Accent 2" w:semiHidden="0" w:uiPriority="63" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 2 Accent 2" w:semiHidden="0" w:uiPriority="64" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 1 Accent 2" w:semiHidden="0" w:uiPriority="65" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 2 Accent 2" w:semiHidden="0" w:uiPriority="66" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 1 Accent 2" w:semiHidden="0" w:uiPriority="67" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 2 Accent 2" w:semiHidden="0" w:uiPriority="68" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 3 Accent 2" w:semiHidden="0" w:uiPriority="69" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Dark List Accent 2" w:semiHidden="0" w:uiPriority="70" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Shading Accent 2" w:semiHidden="0" w:uiPriority="71" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful List Accent 2" w:semiHidden="0" w:uiPriority="72" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Grid Accent 2" w:semiHidden="0" w:uiPriority="73" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Shading Accent 3" w:semiHidden="0" w:uiPriority="60" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light List Accent 3" w:semiHidden="0" w:uiPriority="61" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Grid Accent 3" w:semiHidden="0" w:uiPriority="62" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 1 Accent 3" w:semiHidden="0" w:uiPriority="63" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 2 Accent 3" w:semiHidden="0" w:uiPriority="64" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 1 Accent 3" w:semiHidden="0" w:uiPriority="65" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 2 Accent 3" w:semiHidden="0" w:uiPriority="66" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 1 Accent 3" w:semiHidden="0" w:uiPriority="67" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 2 Accent 3" w:semiHidden="0" w:uiPriority="68" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 3 Accent 3" w:semiHidden="0" w:uiPriority="69" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Dark List Accent 3" w:semiHidden="0" w:uiPriority="70" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Shading Accent 3" w:semiHidden="0" w:uiPriority="71" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful List Accent 3" w:semiHidden="0" w:uiPriority="72" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Grid Accent 3" w:semiHidden="0" w:uiPriority="73" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Shading Accent 4" w:semiHidden="0" w:uiPriority="60" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light List Accent 4" w:semiHidden="0" w:uiPriority="61" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Grid Accent 4" w:semiHidden="0" w:uiPriority="62" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 1 Accent 4" w:semiHidden="0" w:uiPriority="63" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 2 Accent 4" w:semiHidden="0" w:uiPriority="64" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 1 Accent 4" w:semiHidden="0" w:uiPriority="65" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 2 Accent 4" w:semiHidden="0" w:uiPriority="66" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 1 Accent 4" w:semiHidden="0" w:uiPriority="67" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 2 Accent 4" w:semiHidden="0" w:uiPriority="68" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 3 Accent 4" w:semiHidden="0" w:uiPriority="69" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Dark List Accent 4" w:semiHidden="0" w:uiPriority="70" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Shading Accent 4" w:semiHidden="0" w:uiPriority="71" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful List Accent 4" w:semiHidden="0" w:uiPriority="72" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Grid Accent 4" w:semiHidden="0" w:uiPriority="73" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Shading Accent 5" w:semiHidden="0" w:uiPriority="60" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light List Accent 5" w:semiHidden="0" w:uiPriority="61" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Grid Accent 5" w:semiHidden="0" w:uiPriority="62" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 1 Accent 5" w:semiHidden="0" w:uiPriority="63" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 2 Accent 5" w:semiHidden="0" w:uiPriority="64" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 1 Accent 5" w:semiHidden="0" w:uiPriority="65" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 2 Accent 5" w:semiHidden="0" w:uiPriority="66" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 1 Accent 5" w:semiHidden="0" w:uiPriority="67" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 2 Accent 5" w:semiHidden="0" w:uiPriority="68" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 3 Accent 5" w:semiHidden="0" w:uiPriority="69" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Dark List Accent 5" w:semiHidden="0" w:uiPriority="70" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Shading Accent 5" w:semiHidden="0" w:uiPriority="71" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful List Accent 5" w:semiHidden="0" w:uiPriority="72" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Grid Accent 5" w:semiHidden="0" w:uiPriority="73" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Shading Accent 6" w:semiHidden="0" w:uiPriority="60" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light List Accent 6" w:semiHidden="0" w:uiPriority="61" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Light Grid Accent 6" w:semiHidden="0" w:uiPriority="62" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 1 Accent 6" w:semiHidden="0" w:uiPriority="63" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Shading 2 Accent 6" w:semiHidden="0" w:uiPriority="64" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 1 Accent 6" w:semiHidden="0" w:uiPriority="65" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium List 2 Accent 6" w:semiHidden="0" w:uiPriority="66" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 1 Accent 6" w:semiHidden="0" w:uiPriority="67" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 2 Accent 6" w:semiHidden="0" w:uiPriority="68" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Medium Grid 3 Accent 6" w:semiHidden="0" w:uiPriority="69" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Dark List Accent 6" w:semiHidden="0" w:uiPriority="70" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Shading Accent 6" w:semiHidden="0" w:uiPriority="71" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful List Accent 6" w:semiHidden="0" w:uiPriority="72" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Colorful Grid Accent 6" w:semiHidden="0" w:uiPriority="73" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Subtle Emphasis" w:qFormat="1" w:semiHidden="0" w:uiPriority="19" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Intense Emphasis" w:qFormat="1" w:semiHidden="0" w:uiPriority="21" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Subtle Reference" w:qFormat="1" w:semiHidden="0" w:uiPriority="31" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Intense Reference" w:qFormat="1" w:semiHidden="0" w:uiPriority="32" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Book Title" w:qFormat="1" w:semiHidden="0" w:uiPriority="33" w:unhideWhenUsed="0"/>
					<w:lsdException w:name="Bibliography" w:uiPriority="37"/>
					<w:lsdException w:name="TOC Heading" w:qFormat="1" w:uiPriority="39"/>
				</w:latentStyles>
				<w:style w:default="1" w:styleId="a" w:type="paragraph">
					<w:name w:val="Normal"/>
					<w:qFormat/>
					<w:rsid w:val="001A2636"/>
					<w:pPr>
						<w:widowControl w:val="0"/>
						<w:jc w:val="both"/>
					</w:pPr>
					<w:rPr>
						<w:kern w:val="2"/>
						<w:sz w:val="21"/>
						<w:szCs w:val="22"/>
					</w:rPr>
				</w:style>
				<w:style w:default="1" w:styleId="a0" w:type="character">
					<w:name w:val="Default Paragraph Font"/>
					<w:uiPriority w:val="1"/>
					<w:semiHidden/>
					<w:unhideWhenUsed/>
				</w:style>
				<w:style w:default="1" w:styleId="a1" w:type="table">
					<w:name w:val="Normal Table"/>
					<w:uiPriority w:val="99"/>
					<w:semiHidden/>
					<w:unhideWhenUsed/>
					<w:qFormat/>
					<w:tblPr>
						<w:tblInd w:type="dxa" w:w="0"/>
						<w:tblCellMar>
							<w:top w:type="dxa" w:w="0"/>
							<w:left w:type="dxa" w:w="108"/>
							<w:bottom w:type="dxa" w:w="0"/>
							<w:right w:type="dxa" w:w="108"/>
						</w:tblCellMar>
					</w:tblPr>
				</w:style>
				<w:style w:default="1" w:styleId="a2" w:type="numbering">
					<w:name w:val="No List"/>
					<w:uiPriority w:val="99"/>
					<w:semiHidden/>
					<w:unhideWhenUsed/>
				</w:style>
				<w:style w:styleId="a3" w:type="paragraph">
					<w:name w:val="footer"/>
					<w:basedOn w:val="a"/>
					<w:link w:val="Char"/>
					<w:uiPriority w:val="99"/>
					<w:unhideWhenUsed/>
					<w:rsid w:val="001A2636"/>
					<w:pPr>
						<w:tabs>
							<w:tab w:pos="4153" w:val="center"/>
							<w:tab w:pos="8306" w:val="right"/>
						</w:tabs>
						<w:snapToGrid w:val="0"/>
						<w:jc w:val="left"/>
					</w:pPr>
					<w:rPr>
						<w:sz w:val="18"/>
						<w:szCs w:val="18"/>
					</w:rPr>
				</w:style>
				<w:style w:styleId="a4" w:type="paragraph">
					<w:name w:val="header"/>
					<w:basedOn w:val="a"/>
					<w:link w:val="Char0"/>
					<w:uiPriority w:val="99"/>
					<w:unhideWhenUsed/>
					<w:rsid w:val="001A2636"/>
					<w:pPr>
						<w:pBdr>
							<w:bottom w:color="auto" w:space="1" w:sz="6" w:val="single"/>
						</w:pBdr>
						<w:tabs>
							<w:tab w:pos="4153" w:val="center"/>
							<w:tab w:pos="8306" w:val="right"/>
						</w:tabs>
						<w:snapToGrid w:val="0"/>
						<w:jc w:val="center"/>
					</w:pPr>
					<w:rPr>
						<w:sz w:val="18"/>
						<w:szCs w:val="18"/>
					</w:rPr>
				</w:style>
				<w:style w:styleId="a5" w:type="table">
					<w:name w:val="Table Grid"/>
					<w:basedOn w:val="a1"/>
					<w:uiPriority w:val="59"/>
					<w:rsid w:val="001A2636"/>
					<w:tblPr>
						<w:tblInd w:type="dxa" w:w="0"/>
						<w:tblBorders>
							<w:top w:color="auto" w:space="0" w:sz="4" w:val="single"/>
							<w:left w:color="auto" w:space="0" w:sz="4" w:val="single"/>
							<w:bottom w:color="auto" w:space="0" w:sz="4" w:val="single"/>
							<w:right w:color="auto" w:space="0" w:sz="4" w:val="single"/>
							<w:insideH w:color="auto" w:space="0" w:sz="4" w:val="single"/>
							<w:insideV w:color="auto" w:space="0" w:sz="4" w:val="single"/>
						</w:tblBorders>
						<w:tblCellMar>
							<w:top w:type="dxa" w:w="0"/>
							<w:left w:type="dxa" w:w="108"/>
							<w:bottom w:type="dxa" w:w="0"/>
							<w:right w:type="dxa" w:w="108"/>
						</w:tblCellMar>
					</w:tblPr>
				</w:style>
				<w:style w:customStyle="1" w:styleId="1" w:type="paragraph">
					<w:name w:val="列出段落1"/>
					<w:basedOn w:val="a"/>
					<w:uiPriority w:val="34"/>
					<w:qFormat/>
					<w:rsid w:val="001A2636"/>
					<w:pPr>
						<w:ind w:firstLine="420" w:firstLineChars="200"/>
					</w:pPr>
				</w:style>
				<w:style w:customStyle="1" w:styleId="Char0" w:type="character">
					<w:name w:val="页眉 Char"/>
					<w:basedOn w:val="a0"/>
					<w:link w:val="a4"/>
					<w:uiPriority w:val="99"/>
					<w:rsid w:val="001A2636"/>
					<w:rPr>
						<w:sz w:val="18"/>
						<w:szCs w:val="18"/>
					</w:rPr>
				</w:style>
				<w:style w:customStyle="1" w:styleId="Char" w:type="character">
					<w:name w:val="页脚 Char"/>
					<w:basedOn w:val="a0"/>
					<w:link w:val="a3"/>
					<w:uiPriority w:val="99"/>
					<w:qFormat/>
					<w:rsid w:val="001A2636"/>
					<w:rPr>
						<w:sz w:val="18"/>
						<w:szCs w:val="18"/>
					</w:rPr>
				</w:style>
				<w:style w:styleId="a6" w:type="paragraph">
					<w:name w:val="Balloon Text"/>
					<w:basedOn w:val="a"/>
					<w:link w:val="Char1"/>
					<w:uiPriority w:val="99"/>
					<w:semiHidden/>
					<w:unhideWhenUsed/>
					<w:rsid w:val="00E76235"/>
					<w:rPr>
						<w:sz w:val="18"/>
						<w:szCs w:val="18"/>
					</w:rPr>
				</w:style>
				<w:style w:customStyle="1" w:styleId="Char1" w:type="character">
					<w:name w:val="批注框文本 Char"/>
					<w:basedOn w:val="a0"/>
					<w:link w:val="a6"/>
					<w:uiPriority w:val="99"/>
					<w:semiHidden/>
					<w:rsid w:val="00E76235"/>
					<w:rPr>
						<w:kern w:val="2"/>
						<w:sz w:val="18"/>
						<w:szCs w:val="18"/>
					</w:rPr>
				</w:style>
			</w:styles>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-officedocument.customXmlProperties+xml" pkg:name="/customXml/itemProps1.xml" pkg:padding="32">
		<pkg:xmlData pkg:originalXmlStandalone="no">
			<ds:datastoreItem ds:itemID="{B1977F7D-205B-4081-913C-38D41E755F92}" xmlns:ds="http://schemas.openxmlformats.org/officeDocument/2006/customXml">
				<ds:schemaRefs>
					<ds:schemaRef ds:uri="http://www.wps.cn/officeDocument/2013/wpsCustomData"/>
				</ds:schemaRefs>
			</ds:datastoreItem>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-package.relationships+xml" pkg:name="/customXml/_rels/item1.xml.rels" pkg:padding="256">
		<pkg:xmlData>
			<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
				<Relationship Id="rId1" Target="itemProps1.xml" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXmlProps"/>
			</Relationships>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-officedocument.custom-properties+xml" pkg:name="/docProps/custom.xml" pkg:padding="256">
		<pkg:xmlData>
			<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
				<property fmtid="{D5CDD505-2E9C-101B-9397-08002B2CF9AE}" name="KSOProductBuildVer" pid="2">
					<vt:lpwstr>2052-10.1.0.6750</vt:lpwstr>
				</property>
			</Properties>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-package.core-properties+xml" pkg:name="/docProps/core.xml" pkg:padding="256">
		<pkg:xmlData>
			<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				<dc:creator>PCOS.CN</dc:creator>
				<cp:lastModifiedBy>PCOS.CN</cp:lastModifiedBy>
				<cp:revision>2</cp:revision>
				<dcterms:created xsi:type="dcterms:W3CDTF">2017-11-28T06:25:00Z</dcterms:created>
				<dcterms:modified xsi:type="dcterms:W3CDTF">2017-11-28T06:25:00Z</dcterms:modified>
			</cp:coreProperties>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/xml" pkg:name="/customXml/item1.xml" pkg:padding="32">
		<pkg:xmlData>
			<s:customData xmlns="http://www.wps.cn/officeDocument/2013/wpsCustomData" xmlns:s="http://www.wps.cn/officeDocument/2013/wpsCustomData">
				<customSectProps>
					<customSectPr/>
				</customSectProps>
			</s:customData>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml" pkg:name="/word/fontTable.xml">
		<pkg:xmlData>
			<w:fonts xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
				<w:font w:name="Calibri">
					<w:panose1 w:val="020F0502020204030204"/>
					<w:charset w:val="00"/>
					<w:family w:val="swiss"/>
					<w:pitch w:val="variable"/>
					<w:sig w:csb0="0000019F" w:csb1="00000000" w:usb0="E10002FF" w:usb1="4000ACFF" w:usb2="00000009" w:usb3="00000000"/>
				</w:font>
				<w:font w:name="宋体">
					<w:altName w:val="SimSun"/>
					<w:panose1 w:val="02010600030101010101"/>
					<w:charset w:val="86"/>
					<w:family w:val="auto"/>
					<w:pitch w:val="variable"/>
					<w:sig w:csb0="00040001" w:csb1="00000000" w:usb0="00000003" w:usb1="288F0000" w:usb2="00000016" w:usb3="00000000"/>
				</w:font>
				<w:font w:name="Times New Roman">
					<w:panose1 w:val="02020603050405020304"/>
					<w:charset w:val="00"/>
					<w:family w:val="roman"/>
					<w:pitch w:val="variable"/>
					<w:sig w:csb0="000001FF" w:csb1="00000000" w:usb0="E0002AFF" w:usb1="C0007841" w:usb2="00000009" w:usb3="00000000"/>
				</w:font>
				<w:font w:name="微软雅黑">
					<w:panose1 w:val="020B0503020204020204"/>
					<w:charset w:val="86"/>
					<w:family w:val="swiss"/>
					<w:pitch w:val="variable"/>
					<w:sig w:csb0="0004001F" w:csb1="00000000" w:usb0="80000287" w:usb1="280F3C52" w:usb2="00000016" w:usb3="00000000"/>
				</w:font>
				<w:font w:name="Cambria">
					<w:panose1 w:val="02040503050406030204"/>
					<w:charset w:val="00"/>
					<w:family w:val="roman"/>
					<w:pitch w:val="variable"/>
					<w:sig w:csb0="0000019F" w:csb1="00000000" w:usb0="E00002FF" w:usb1="400004FF" w:usb2="00000000" w:usb3="00000000"/>
				</w:font>
			</w:fonts>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml" pkg:name="/word/webSettings.xml">
		<pkg:xmlData>
			<w:webSettings xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"/>
		</pkg:xmlData>
	</pkg:part>
	<pkg:part pkg:contentType="application/vnd.openxmlformats-officedocument.extended-properties+xml" pkg:name="/docProps/app.xml" pkg:padding="256">
		<pkg:xmlData>
			<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
				<Template>Normal.dotm</Template>
				<TotalTime>1</TotalTime>
				<Pages>1</Pages>
				<Words>97</Words>
				<Characters>555</Characters>
				<Application>Microsoft Office Word</Application>
				<DocSecurity>0</DocSecurity>
				<Lines>4</Lines>
				<Paragraphs>1</Paragraphs>
				<ScaleCrop>false</ScaleCrop>
				<Company/>
				<LinksUpToDate>false</LinksUpToDate>
				<CharactersWithSpaces>651</CharactersWithSpaces>
				<SharedDoc>false</SharedDoc>
				<HyperlinksChanged>false</HyperlinksChanged>
				<AppVersion>12.0000</AppVersion>
			</Properties>
		</pkg:xmlData>
	</pkg:part>
</pkg:package>