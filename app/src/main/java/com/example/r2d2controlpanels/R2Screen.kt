package com.example.r2d2controlpanels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

@Composable
fun R2DomeStateCard(
    connectionState: BluetoothState,
    panelsOpen: Boolean,
    holoActive: Boolean,
    rearLogicText: String,
    pan: Float,
    tilt: Float,
    onPanelsToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF101923)
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFF284866)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "> R2-D2 DOME STATE",
                    color = Color(0xFF64C8FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Canvas drawing representing the dome head
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF151D38), Color(0xFF080C18))
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .border(2.dp, Color(0xFF2B4C85).copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val cx = w / 2f
                                val cy = h * 0.75f
                                
                                val x = offset.x
                                val y = offset.y
                                
                                val isLeftPanel = x in (cx - 70.dp.toPx())..(cx - 30.dp.toPx()) &&
                                        y in (cy - 35.dp.toPx())..(cy - 2.dp.toPx())
                                
                                val isRightPanel = x in (cx + 30.dp.toPx())..(cx + 70.dp.toPx()) &&
                                        y in (cy - 35.dp.toPx())..(cy - 2.dp.toPx())
                                        
                                val isRearPanel = x in (cx - 20.dp.toPx())..(cx + 20.dp.toPx()) &&
                                        y in (cy - 75.dp.toPx())..(cy - 40.dp.toPx())
                                        
                                if (isLeftPanel || isRightPanel || isRearPanel) {
                                    onPanelsToggle(!panelsOpen)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Center of the dome base
                    val cx = w / 2f
                    val cy = h * 0.75f
                    val domeRadius = 75.dp.toPx()

                    // 1. Draw projection beam if holoActive is true
                    if (holoActive) {
                        val beamPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(cx, cy - 10.dp.toPx())
                            lineTo(cx - 60.dp.toPx(), h)
                            lineTo(cx + 60.dp.toPx(), h)
                            close()
                        }
                        drawPath(
                            path = beamPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x9964DFDF), Color(0x0064DFDF))
                            )
                        )
                    }

                    // 2. Draw Main Dome Body (arc from 180 degrees, sweep 180)
                    val domeRect = androidx.compose.ui.geometry.Rect(
                        cx - domeRadius,
                        cy - domeRadius,
                        cx + domeRadius,
                        cy + domeRadius
                    )
                    
                    drawArc(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFEAEDF2), Color(0xFF738299))
                        ),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = domeRect.topLeft,
                        size = domeRect.size
                    )
                    
                    // Dome contour outline
                    drawArc(
                        color = Color(0xFF4D5A6E),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = domeRect.topLeft,
                        size = domeRect.size,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // 3. Horizontal Base Rim
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFEAEDF2), Color(0xFF738299))
                        ),
                        topLeft = Offset(cx - domeRadius, cy),
                        size = androidx.compose.ui.geometry.Size(domeRadius * 2, 8.dp.toPx())
                    )
                    drawRect(
                        color = Color(0xFF4D5A6E),
                        topLeft = Offset(cx - domeRadius, cy),
                        size = androidx.compose.ui.geometry.Size(domeRadius * 2, 8.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // Draw dark horizontal separation lines
                    drawLine(
                        color = Color(0xFF222222),
                        start = Offset(cx - domeRadius, cy + 2.dp.toPx()),
                        end = Offset(cx + domeRadius, cy + 2.dp.toPx()),
                        strokeWidth = 2f
                    )

                    // 4. Blue Inlays (Static left/right paths)
                    val blueMetalBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D3C94), Color(0xFF041A4A))
                    )

                    val leftInlay = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - 45.dp.toPx(), cy - 55.dp.toPx())
                        lineTo(cx - 28.dp.toPx(), cy - 52.dp.toPx())
                        lineTo(cx - 28.dp.toPx(), cy - 32.dp.toPx())
                        lineTo(cx - 45.dp.toPx(), cy - 38.dp.toPx())
                        close()
                    }
                    drawPath(path = leftInlay, brush = blueMetalBrush)
                    drawPath(path = leftInlay, color = Color(0xFF222222), style = Stroke(width = 1.dp.toPx()))

                    val rightInlay = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx + 28.dp.toPx(), cy - 52.dp.toPx())
                        lineTo(cx + 45.dp.toPx(), cy - 55.dp.toPx())
                        lineTo(cx + 45.dp.toPx(), cy - 38.dp.toPx())
                        lineTo(cx + 28.dp.toPx(), cy - 32.dp.toPx())
                        close()
                    }
                    drawPath(path = rightInlay, brush = blueMetalBrush)
                    drawPath(path = rightInlay, color = Color(0xFF222222), style = Stroke(width = 1.dp.toPx()))

                    // 5. Dynamic Pie Panels (open/close animation shifts)
                    val panelShiftY = if (panelsOpen) -12.dp.toPx() else 0f
                    val panelColor = if (panelsOpen) Color(0xFF7D8B9C) else Color(0xFF9BA8B8)

                    // Top Left panel
                    val topLeftPanel = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - 65.dp.toPx(), cy - 20.dp.toPx() + panelShiftY)
                        lineTo(cx - 38.dp.toPx(), cy - 20.dp.toPx() + panelShiftY)
                        lineTo(cx - 38.dp.toPx(), cy - 5.dp.toPx() + panelShiftY)
                        lineTo(cx - 65.dp.toPx(), cy - 10.dp.toPx() + panelShiftY)
                        close()
                    }
                    drawPath(path = topLeftPanel, color = panelColor)
                    drawPath(path = topLeftPanel, color = Color(0xFF4D5A6E), style = Stroke(width = 1.5.dp.toPx()))

                    // Top Right panel
                    val topRightPanel = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx + 38.dp.toPx(), cy - 20.dp.toPx() + panelShiftY)
                        lineTo(cx + 65.dp.toPx(), cy - 20.dp.toPx() + panelShiftY)
                        lineTo(cx + 65.dp.toPx(), cy - 10.dp.toPx() + panelShiftY)
                        lineTo(cx + 38.dp.toPx(), cy - 5.dp.toPx() + panelShiftY)
                        close()
                    }
                    drawPath(path = topRightPanel, color = panelColor)
                    drawPath(path = topRightPanel, color = Color(0xFF4D5A6E), style = Stroke(width = 1.5.dp.toPx()))

                    // Rear Center High panel
                    val rearPanel = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - 18.dp.toPx(), cy - 62.dp.toPx() + panelShiftY)
                        lineTo(cx + 18.dp.toPx(), cy - 62.dp.toPx() + panelShiftY)
                        lineTo(cx + 12.dp.toPx(), cy - 48.dp.toPx() + panelShiftY)
                        lineTo(cx - 12.dp.toPx(), cy - 48.dp.toPx() + panelShiftY)
                        close()
                    }
                    drawPath(path = rearPanel, color = panelColor)
                    drawPath(path = rearPanel, color = Color(0xFF4D5A6E), style = Stroke(width = 1.5.dp.toPx()))

                    // 6. Main LED (PSI) Status Display
                    drawCircle(
                        color = Color(0xFF111111),
                        radius = 8.dp.toPx(),
                        center = Offset(cx, cy - 25.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF333333),
                        radius = 8.dp.toPx(),
                        center = Offset(cx, cy - 25.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    
                    val statusLedColor = when (connectionState) {
                        BluetoothState.CONNECTED -> Color(0xFF0055FF) // Solid blue lens glow
                        BluetoothState.CONNECTING -> Color(0xFFFFD54F)
                        BluetoothState.DISCONNECTED -> Color(0xFFFF5252)
                    }
                    drawCircle(
                        color = statusLedColor,
                        radius = 5.dp.toPx(),
                        center = Offset(cx, cy - 25.dp.toPx())
                    )
                    // Core light highlight
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = 2.dp.toPx(),
                        center = Offset(cx, cy - 25.dp.toPx())
                    )

                    // 7. Front Logic Display Box showing "R2-D2 OK" text
                    drawRect(
                        color = Color(0xFF050811),
                        topLeft = Offset(cx - 30.dp.toPx(), cy - 45.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 12.dp.toPx())
                    )
                    drawRect(
                        color = Color(0xFF2A3D5E),
                        topLeft = Offset(cx - 30.dp.toPx(), cy - 45.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 12.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // 8. Holo Projector Eyeball
                    // Socket base
                    drawOval(
                        color = Color(0xFF1D2F4D),
                        topLeft = Offset(cx - 12.dp.toPx(), cy - 8.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 8.dp.toPx())
                    )
                    drawOval(
                        color = Color(0xFF111111),
                        topLeft = Offset(cx - 12.dp.toPx(), cy - 8.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 8.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Moving eyeball eyeball lens center responds slightly to pan/tilt offset
                    val offsetX = ((pan - 90f) / 90f) * 4.dp.toPx()
                    val offsetY = ((tilt - 90f) / 90f) * 3.dp.toPx()

                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFEAEDF2), Color(0xFF738299))
                        ),
                        radius = 7.dp.toPx(),
                        center = Offset(cx + offsetX, cy - 8.dp.toPx() + offsetY)
                    )
                    drawCircle(
                        color = Color(0xFF111111),
                        radius = 7.dp.toPx(),
                        center = Offset(cx + offsetX, cy - 8.dp.toPx() + offsetY),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Pupil & active glow
                    drawCircle(
                        color = Color(0xFF111111),
                        radius = 4.dp.toPx(),
                        center = Offset(cx + offsetX, cy - 8.dp.toPx() + offsetY)
                    )
                    drawCircle(
                        color = if (holoActive) Color(0xFF64DFDF) else Color(0x3364DFDF),
                        radius = 2.dp.toPx(),
                        center = Offset(cx + offsetX, cy - 8.dp.toPx() + offsetY)
                    )
                }

                // Front logic text display drawn as overlay
                Text(
                    text = "R2-D2 OK",
                    color = Color(0xFF52ECFF),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.Center).offset(y = (-39).dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Overlay panel control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onPanelsToggle(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Text("OPEN DOME", fontSize = 12.sp)
                }

                Button(
                    onClick = { onPanelsToggle(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF455A64)
                    )
                ) {
                    Text("CLOSE DOME", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rear logic display monitor matching style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090E17), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF1F2F45), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rear Logic Display Emulator:",
                    color = Color(0xFF8FA0C0),
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF11051A), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF8E00D9), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (rearLogicText.isEmpty()) "SYS-ONLINE" else rearLogicText,
                        color = Color(0xFFDF4DFF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun R2BodyStateCard(
    connectionState: BluetoothState,
    gripperOpen: Boolean,
    interfaceOpen: Boolean,
    chargeOpen: Boolean,
    dataOpen: Boolean,
    onGripperToggle: (Boolean) -> Unit,
    onInterfaceToggle: (Boolean) -> Unit,
    onChargeToggle: (Boolean) -> Unit,
    onDataToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF101923)
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFF284866)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "> R2-D2 BODY STATE",
                    color = Color(0xFF64C8FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Canvas drawing representing the body controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF151D38), Color(0xFF080C18))
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .border(2.dp, Color(0xFF2B4C85).copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val cx = w / 2f
                                val cy = h * 0.5f
                                val x = offset.x
                                val y = offset.y

                                val isLeft = x in (cx - 60.dp.toPx())..(cx - 30.dp.toPx()) &&
                                        y in (cy - 90.dp.toPx())..(cy - 10.dp.toPx())
                                val isRight = x in (cx + 30.dp.toPx())..(cx + 60.dp.toPx()) &&
                                        y in (cy - 90.dp.toPx())..(cy - 10.dp.toPx())
                                val isCharge = x in (cx - 28.dp.toPx())..(cx - 2.dp.toPx()) &&
                                        y in (cy - 35.dp.toPx())..(cy - 5.dp.toPx())
                                val isData = x in (cx + 2.dp.toPx())..(cx + 28.dp.toPx()) &&
                                        y in (cy - 35.dp.toPx())..(cy - 5.dp.toPx())

                                if (isLeft) onGripperToggle(!gripperOpen)
                                else if (isRight) onInterfaceToggle(!interfaceOpen)
                                else if (isCharge) onChargeToggle(!chargeOpen)
                                else if (isData) onDataToggle(!dataOpen)
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h * 0.5f

                    val bodyWidth = 110.dp.toPx()
                    val bodyHeight = 160.dp.toPx()

                    // Draw main body cylindrical shape (white with silver borders)
                    val bodyRect = androidx.compose.ui.geometry.Rect(
                        cx - bodyWidth / 2f,
                        cy - bodyHeight / 2f,
                        cx + bodyWidth / 2f,
                        cy + bodyHeight / 2f
                    )
                    
                    // Draw outer body fill
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFEAEDF2), Color(0xFFD0D7E3))
                        ),
                        topLeft = bodyRect.topLeft,
                        size = bodyRect.size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )
                    
                    // Body outline
                    drawRoundRect(
                        color = Color(0xFF4D5A6E),
                        topLeft = bodyRect.topLeft,
                        size = bodyRect.size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw body panel details
                    drawRect(
                        color = Color(0xFF0D3C94),
                        topLeft = Offset(cx - 30.dp.toPx(), cy - 75.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 14.dp.toPx())
                    )
                    
                    // 1. Left Large Panel (Gripper)
                    val leftPanelX = cx - 45.dp.toPx()
                    val leftPanelY = cy - 50.dp.toPx()
                    val leftPanelW = 16.dp.toPx()
                    val leftPanelH = 65.dp.toPx()

                    if (gripperOpen) {
                        // Swing door 1 open to the left
                        val doorPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(leftPanelX, leftPanelY)
                            lineTo(leftPanelX - 18.dp.toPx(), leftPanelY - 5.dp.toPx())
                            lineTo(leftPanelX - 18.dp.toPx(), leftPanelY + leftPanelH + 5.dp.toPx())
                            lineTo(leftPanelX, leftPanelY + leftPanelH)
                            close()
                        }
                        drawPath(path = doorPath, color = Color(0xFFB8C2D1))
                        drawPath(path = doorPath, color = Color(0xFF4D5A6E), style = Stroke(width = 1.5.dp.toPx()))

                        // Draw deployed Gripper Arm (3)
                        drawLine(
                            color = Color(0xFF738299),
                            start = Offset(leftPanelX + leftPanelW/2f, leftPanelY + 10.dp.toPx()),
                            end = Offset(leftPanelX - 12.dp.toPx(), leftPanelY + 35.dp.toPx()),
                            strokeWidth = 4.dp.toPx()
                        )
                        // Claw hook details
                        drawCircle(
                            color = Color(0xFF222222),
                            radius = 4.dp.toPx(),
                            center = Offset(leftPanelX - 12.dp.toPx(), leftPanelY + 35.dp.toPx())
                        )
                    } else {
                        // Closed door
                        drawRect(
                            color = Color(0xFF0D3C94),
                            topLeft = Offset(leftPanelX, leftPanelY),
                            size = androidx.compose.ui.geometry.Size(leftPanelW, leftPanelH)
                        )
                        drawRect(
                            color = Color(0xFF4D5A6E),
                            topLeft = Offset(leftPanelX, leftPanelY),
                            size = androidx.compose.ui.geometry.Size(leftPanelW, leftPanelH),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // 2. Right Large Panel (Interface Arm)
                    val rightPanelX = cx + 29.dp.toPx()
                    val rightPanelY = cy - 50.dp.toPx()
                    val rightPanelW = 16.dp.toPx()
                    val rightPanelH = 65.dp.toPx()

                    if (interfaceOpen) {
                        // Swing door 5 open to the right
                        val doorPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(rightPanelX + rightPanelW, rightPanelY)
                            lineTo(rightPanelX + rightPanelW + 18.dp.toPx(), rightPanelY - 5.dp.toPx())
                            lineTo(rightPanelX + rightPanelW + 18.dp.toPx(), rightPanelY + rightPanelH + 5.dp.toPx())
                            lineTo(rightPanelX + rightPanelW, rightPanelY + rightPanelH)
                            close()
                        }
                        drawPath(path = doorPath, color = Color(0xFFB8C2D1))
                        drawPath(path = doorPath, color = Color(0xFF4D5A6E), style = Stroke(width = 1.5.dp.toPx()))

                        // Draw deployed Interface Arm (8)
                        drawLine(
                            color = Color(0xFF738299),
                            start = Offset(rightPanelX + rightPanelW/2f, rightPanelY + 10.dp.toPx()),
                            end = Offset(rightPanelX + rightPanelW + 12.dp.toPx(), rightPanelY + 40.dp.toPx()),
                            strokeWidth = 3.dp.toPx()
                        )
                        drawCircle(
                            color = Color(0xFF00FF99),
                            radius = 3.dp.toPx(),
                            center = Offset(rightPanelX + rightPanelW + 12.dp.toPx(), rightPanelY + 40.dp.toPx())
                        )
                    } else {
                        // Closed door
                        drawRect(
                            color = Color(0xFF0D3C94),
                            topLeft = Offset(rightPanelX, rightPanelY),
                            size = androidx.compose.ui.geometry.Size(rightPanelW, rightPanelH)
                        )
                        drawRect(
                            color = Color(0xFF4D5A6E),
                            topLeft = Offset(rightPanelX, rightPanelY),
                            size = androidx.compose.ui.geometry.Size(rightPanelW, rightPanelH),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // 3. Charge Door (2) & Interior (4)
                    val chargeX = cx - 22.dp.toPx()
                    val chargeY = cy - 25.dp.toPx()
                    val chargeW = 18.dp.toPx()
                    val chargeH = 20.dp.toPx()

                    if (chargeOpen) {
                        // Draw slot background (4)
                        drawRect(
                            color = Color(0xFF111111),
                            topLeft = Offset(chargeX, chargeY),
                            size = androidx.compose.ui.geometry.Size(chargeW, chargeH)
                        )
                        // Red LEDs
                        drawCircle(color = Color(0xFFFF0022), radius = 2.dp.toPx(), center = Offset(chargeX + 5.dp.toPx(), chargeY + 5.dp.toPx()))
                        drawCircle(color = Color(0xFFFF0022), radius = 2.dp.toPx(), center = Offset(chargeX + 5.dp.toPx(), chargeY + 12.dp.toPx()))
                        // USB ports
                        drawRect(color = Color(0xFF738299), topLeft = Offset(chargeX + 10.dp.toPx(), chargeY + 4.dp.toPx()), size = androidx.compose.ui.geometry.Size(6.dp.toPx(), 4.dp.toPx()))
                        drawRect(color = Color(0xFF738299), topLeft = Offset(chargeX + 10.dp.toPx(), chargeY + 11.dp.toPx()), size = androidx.compose.ui.geometry.Size(6.dp.toPx(), 4.dp.toPx()))

                        // Swung door (2)
                        val doorPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(chargeX, chargeY)
                            lineTo(chargeX - 12.dp.toPx(), chargeY - 2.dp.toPx())
                            lineTo(chargeX - 12.dp.toPx(), chargeY + chargeH + 2.dp.toPx())
                            lineTo(chargeX, chargeY + chargeH)
                            close()
                        }
                        drawPath(path = doorPath, color = Color(0xFFB8C2D1))
                        drawPath(path = doorPath, color = Color(0xFF4D5A6E), style = Stroke(width = 1.dp.toPx()))
                    } else {
                        // Closed door
                        drawRect(
                            color = Color(0xFF9BA8B8),
                            topLeft = Offset(chargeX, chargeY),
                            size = androidx.compose.ui.geometry.Size(chargeW, chargeH)
                        )
                        drawRect(
                            color = Color(0xFF4D5A6E),
                            topLeft = Offset(chargeX, chargeY),
                            size = androidx.compose.ui.geometry.Size(chargeW, chargeH),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // 4. Data Port / Diagnostic Door (6) & Matrix (7)
                    val dataX = cx + 4.dp.toPx()
                    val dataY = cy - 25.dp.toPx()
                    val dataW = 18.dp.toPx()
                    val dataH = 20.dp.toPx()

                    if (dataOpen) {
                        // Draw slot background (7)
                        drawRect(
                            color = Color(0xFF111111),
                            topLeft = Offset(dataX, dataY),
                            size = androidx.compose.ui.geometry.Size(dataW, dataH)
                        )
                        // Status Matrix indicator bars
                        drawRect(color = Color(0xFFFFD54F), topLeft = Offset(dataX + 3.dp.toPx(), dataY + 3.dp.toPx()), size = androidx.compose.ui.geometry.Size(5.dp.toPx(), 3.dp.toPx()))
                        drawRect(color = Color(0xFF00FF99), topLeft = Offset(dataX + 3.dp.toPx(), dataY + 8.dp.toPx()), size = androidx.compose.ui.geometry.Size(5.dp.toPx(), 3.dp.toPx()))
                        drawRect(color = Color(0xFF0055FF), topLeft = Offset(dataX + 10.dp.toPx(), dataY + 5.dp.toPx()), size = androidx.compose.ui.geometry.Size(4.dp.toPx(), 10.dp.toPx()))

                        // Swung door (6)
                        val doorPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(dataX + dataW, dataY)
                            lineTo(dataX + dataW + 12.dp.toPx(), dataY - 2.dp.toPx())
                            lineTo(dataX + dataW + 12.dp.toPx(), dataY + dataH + 2.dp.toPx())
                            lineTo(dataX + dataW, dataY + dataH)
                            close()
                        }
                        drawPath(path = doorPath, color = Color(0xFFB8C2D1))
                        drawPath(path = doorPath, color = Color(0xFF4D5A6E), style = Stroke(width = 1.dp.toPx()))
                    } else {
                        // Closed door
                        drawRect(
                            color = Color(0xFF9BA8B8),
                            topLeft = Offset(dataX, dataY),
                            size = androidx.compose.ui.geometry.Size(dataW, dataH)
                        )
                        drawRect(
                            color = Color(0xFF4D5A6E),
                            topLeft = Offset(dataX, dataY),
                            size = androidx.compose.ui.geometry.Size(dataW, dataH),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body control buttons grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onGripperToggle(!gripperOpen) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gripperOpen) Color(0xFF1976D2) else Color(0xFF455A64)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = if (gripperOpen) "CLOSE GRIPPER" else "OPEN GRIPPER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onInterfaceToggle(!interfaceOpen) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (interfaceOpen) Color(0xFF1976D2) else Color(0xFF455A64)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = if (interfaceOpen) "CLOSE INTERFACE" else "OPEN INTERFACE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onChargeToggle(!chargeOpen) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (chargeOpen) Color(0xFF1976D2) else Color(0xFF455A64)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = if (chargeOpen) "CLOSE CHARGE" else "OPEN CHARGE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onDataToggle(!dataOpen) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dataOpen) Color(0xFF1976D2) else Color(0xFF455A64)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = if (dataOpen) "CLOSE DATA PORT" else "OPEN DATA PORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun R2Screen(
    bluetoothController: BluetoothController
) {

    // ==================================================
    // JOYSTICK
    // ==================================================

    val knobX = remember {
        mutableFloatStateOf(0f)
    }

    val knobY = remember {
        mutableFloatStateOf(0f)
    }

    val pan = remember {
        mutableFloatStateOf(90f)
    }

    val tilt = remember {
        mutableFloatStateOf(90f)
    }

    // ==================================================
    // RLD MESSAGE
    // ==================================================

    val rearLogicMessage = remember {
        mutableStateOf("")
    }

    // ==================================================
    // HOISTED STATES FOR R2 DOME & BODY STATE
    // ==================================================

    val panelsOpen = remember {
        mutableStateOf(false)
    }

    val holoLightsEnabled = remember {
        mutableStateOf(true)
    }

    val frontHoloOn = remember {
        mutableStateOf(true)
    }

    val topHoloOn = remember {
        mutableStateOf(true)
    }

    val rearHoloOn = remember {
        mutableStateOf(true)
    }

    // ==================================================
    // BODY STATES
    // ==================================================

    val gripperOpen = remember {
        mutableStateOf(false)
    }

    val interfaceOpen = remember {
        mutableStateOf(false)
    }

    val chargeOpen = remember {
        mutableStateOf(false)
    }

    val dataOpen = remember {
        mutableStateOf(false)
    }

    // ==================================================
    // SEND HOLO
    // ==================================================

    fun sendHolo() {

        bluetoothController.send(
            "HOLO:${pan.floatValue.toInt()},${tilt.floatValue.toInt()}\n"
        )
    }

    // ==================================================
    // ROOT
    // ==================================================

    Box(

        modifier = Modifier
            .fillMaxSize()

            .background(

                Brush.verticalGradient(

                    colors = listOf(

                        Color(0xFF010409),

                        Color(0xFF07111F),

                        Color(0xFF0A1B2D),

                        Color(0xFF000000)
                    )
                )
            )
    ) {

        val scrollState =
            rememberScrollState()

        Column(

            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .imePadding()
                .padding(20.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            // ==================================================
            // TITLE
            // ==================================================

            Text(

                text = "R2 ASTROMECH CONTROL",

                color = Color(0xFF64C8FF),

                fontSize = 30.sp,

                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(

                text = "DOME OPERATIONS INTERFACE",

                color = Color.LightGray,

                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // ==================================================
            // CONNECTION STATUS
            // ==================================================

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(

                        containerColor =
                            Color(0xFF101923)
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        Color(0xFF284866)
                    ),

                shape =
                    RoundedCornerShape(20.dp)
            ) {

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    val statusColor =
                        when (
                            bluetoothController.connectionState
                        ) {

                            BluetoothState.CONNECTED ->
                                Color(0xFF00FF99)

                            BluetoothState.CONNECTING ->
                                Color(0xFFFFD54F)

                            BluetoothState.DISCONNECTED ->
                                Color(0xFFFF5252)
                        }

                    val statusText =
                        when (
                            bluetoothController.connectionState
                        ) {

                            BluetoothState.CONNECTED ->
                                "R2 LINK ESTABLISHED"

                            BluetoothState.CONNECTING ->
                                "CONNECTING TO DOME..."

                            BluetoothState.DISCONNECTED ->
                                "DOME OFFLINE"
                        }

                    Canvas(
                        modifier = Modifier.size(18.dp)
                    ) {

                        drawCircle(
                            color = statusColor
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Text(

                        text = statusText,

                        color = Color.White,

                        fontSize = 16.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // ==================================================
            // R2-D2 DOME STATE VISUALIZER
            // ==================================================

            R2DomeStateCard(
                connectionState = bluetoothController.connectionState,
                panelsOpen = panelsOpen.value,
                holoActive = holoLightsEnabled.value,
                rearLogicText = rearLogicMessage.value,
                pan = pan.floatValue,
                tilt = tilt.floatValue,
                onPanelsToggle = { open ->
                    panelsOpen.value = open
                    if (open) {
                        bluetoothController.send("OPEN_TOP\n")
                    } else {
                        bluetoothController.send("CLOSE_TOP\n")
                    }
                }
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // ==================================================
            // R2-D2 BODY STATE VISUALIZER
            // ==================================================

            R2BodyStateCard(
                connectionState = bluetoothController.connectionState,
                gripperOpen = gripperOpen.value,
                interfaceOpen = interfaceOpen.value,
                chargeOpen = chargeOpen.value,
                dataOpen = dataOpen.value,
                onGripperToggle = { open ->
                    gripperOpen.value = open
                    if (open) {
                        bluetoothController.send("GRIPPER_OPEN\n")
                        bluetoothController.send("ARM_A_DEPLOY\n")
                    } else {
                        bluetoothController.send("GRIPPER_CLOSE\n")
                        bluetoothController.send("ARM_A_RETRACT\n")
                    }
                },
                onInterfaceToggle = { open ->
                    interfaceOpen.value = open
                    if (open) {
                        bluetoothController.send("ARM_B_DEPLOY\n")
                    } else {
                        bluetoothController.send("ARM_B_RETRACT\n")
                    }
                },
                onChargeToggle = { open ->
                    chargeOpen.value = open
                    if (open) {
                        bluetoothController.send("CHARGE_OPEN\n")
                    } else {
                        bluetoothController.send("CHARGE_CLOSE\n")
                    }
                },
                onDataToggle = { open ->
                    dataOpen.value = open
                    if (open) {
                        bluetoothController.send("DATA_OPEN\n")
                    } else {
                        bluetoothController.send("DATA_CLOSE\n")
                    }
                }
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // ==================================================
            // JOYSTICK
            // ==================================================

            Card(

                shape =
                    RoundedCornerShape(28.dp),

                colors =
                    CardDefaults.cardColors(

                        containerColor =
                            Color(0xFF0E1724)
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        Color(0xFF2A5E8A)
                    )
            ) {

                Column(

                    modifier =
                        Modifier.padding(24.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(

                        text = "HOLOPROJECTOR CONTROL",

                        color = Color(0xFF64C8FF),

                        fontSize = 18.sp,

                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Canvas(

                        modifier = Modifier

                            .size(240.dp)

                            .border(

                                width = 2.dp,

                                color = Color(0xFF4DA3FF),

                                shape = CircleShape
                            )

                            .pointerInput(Unit) {

                                detectDragGestures(

                                    onDragEnd = {

                                        knobX.floatValue = 0f
                                        knobY.floatValue = 0f

                                        pan.floatValue = 90f
                                        tilt.floatValue = 90f

                                        sendHolo()
                                    }

                                ) { change, dragAmount ->

                                    change.consume()

                                    val maxRadius = 100f

                                    val newX =
                                        knobX.floatValue +
                                                dragAmount.x

                                    val newY =
                                        knobY.floatValue +
                                                dragAmount.y

                                    val distance =
                                        sqrt(
                                            newX * newX +
                                                    newY * newY
                                        )

                                    if (distance <= maxRadius) {

                                        knobX.floatValue = newX
                                        knobY.floatValue = newY

                                        // FIXED DIRECTION

                                        pan.floatValue =
                                            ((newX / maxRadius) * 90f + 90f)
                                                .coerceIn(0f, 180f)

                                        tilt.floatValue =
                                            ((newY / maxRadius) * 90f + 90f)
                                                .coerceIn(0f, 180f)

                                        sendHolo()
                                    }
                                }
                            }

                    ) {

                        val center =
                            Offset(
                                size.width / 2,
                                size.height / 2
                            )

                        // ==================================================
                        // OUTER
                        // ==================================================

                        drawCircle(
                            color = Color(0xFF13263D),
                            radius = size.width / 2
                        )

                        drawCircle(

                            color = Color(0xFF2A7FFF),

                            radius = size.width / 2,

                            style = Stroke(
                                width = 6f
                            )
                        )

                        // ==================================================
                        // CROSSHAIR
                        // ==================================================

                        drawLine(

                            color = Color(0x552A7FFF),

                            start =
                                Offset(center.x, 0f),

                            end =
                                Offset(center.x, size.height),

                            strokeWidth = 3f
                        )

                        drawLine(

                            color = Color(0x552A7FFF),

                            start =
                                Offset(0f, center.y),

                            end =
                                Offset(size.width, center.y),

                            strokeWidth = 3f
                        )

                        // ==================================================
                        // KNOB
                        // ==================================================

                        drawCircle(

                            color = Color(0xFF64C8FF),

                            radius = 42f,

                            center = Offset(

                                center.x +
                                        knobX.floatValue,

                                center.y +
                                        knobY.floatValue
                            )
                        )

                        drawCircle(

                            color = Color.White,

                            radius = 14f,

                            center = Offset(

                                center.x +
                                        knobX.floatValue,

                                center.y +
                                        knobY.floatValue
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    HorizontalDivider(
                        color = Color(0xFF284866)
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Row(

                        horizontalArrangement =
                            Arrangement.SpaceEvenly,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Column(

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "PAN",
                                color = Color.Gray
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(

                                text =
                                    pan.floatValue.toInt().toString(),

                                color = Color.White,

                                fontSize = 28.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Column(

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "TILT",
                                color = Color.Gray
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(

                                text =
                                    tilt.floatValue.toInt().toString(),

                                color = Color.White,

                                fontSize = 28.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // ==================================================
            // PANEL CONTROLS
            // ==================================================

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(20.dp),

                colors =
                    CardDefaults.cardColors(

                        containerColor =
                            Color(0xFF101923)
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        Color(0xFF284866)
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(

                        text = "PIE PANEL SYSTEM",

                        color = Color(0xFF64C8FF),

                        fontSize = 18.sp,

                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Row(

                        horizontalArrangement =
                            Arrangement.SpaceEvenly,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        repeat(4) {

                            Canvas(
                                modifier =
                                    Modifier.size(26.dp)
                            ) {

                                drawCircle(

                                    color =
                                        if (
                                            bluetoothController.isConnected
                                        )
                                            Color(0xFF00E5FF)
                                        else
                                            Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    Row(

                        horizontalArrangement =
                            Arrangement.spacedBy(16.dp),

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Button(

                            modifier =
                                Modifier.weight(1f),

                            enabled =
                                bluetoothController.isConnected,

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Color(0xFF1976D2)
                                ),

                            onClick = {
                                panelsOpen.value = true
                                bluetoothController.send(
                                    "OPEN_TOP\n"
                                )
                            }
                        ) {

                            Text("OPEN")
                        }

                        Button(

                            modifier =
                                Modifier.weight(1f),

                            enabled =
                                bluetoothController.isConnected,

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Color(0xFF455A64)
                                ),

                            onClick = {
                                panelsOpen.value = false
                                bluetoothController.send(
                                    "CLOSE_TOP\n"
                                )
                            }
                        ) {

                            Text("CLOSE")
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

// ==================================================
// HOLO LIGHTS
// ==================================================

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color(0xFF101923)
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        Color(0xFF284866)
                    ),

                shape =
                    RoundedCornerShape(20.dp)
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(

                        text = "ASTROPIXEL HOLO SYSTEM",

                        color = Color(0xFF64C8FF),

                        fontSize = 18.sp,

                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

// ==========================================
// FRONT / TOP / REAR TOGGLE SWITCHES
// ==========================================

                    Column(

                        verticalArrangement =
                            Arrangement.spacedBy(16.dp),

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        // ======================================
                        // FRONT HOLO
                        // ======================================

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(

                                text = "FRONT HOLO",

                                color = Color.White,

                                fontSize = 16.sp,

                                fontWeight = FontWeight.Bold
                            )

                            Switch(

                                checked =
                                    frontHoloOn.value,

                                enabled =
                                    bluetoothController.isConnected,

                                onCheckedChange = { enabled ->

                                    frontHoloOn.value =
                                        enabled

                                    if (enabled) {

                                        bluetoothController.send(
                                            "FRONT_HOLO\n"
                                        )

                                    } else {

                                        bluetoothController.send(
                                            "FRONT_HOLO_OFF\n"
                                        )
                                    }
                                }
                            )
                        }

                        // ======================================
                        // TOP HOLO
                        // ======================================

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(

                                text = "TOP HOLO",

                                color = Color.White,

                                fontSize = 16.sp,

                                fontWeight = FontWeight.Bold
                            )

                            Switch(

                                checked =
                                    topHoloOn.value,

                                enabled =
                                    bluetoothController.isConnected,

                                onCheckedChange = { enabled ->

                                    topHoloOn.value =
                                        enabled

                                    if (enabled) {

                                        bluetoothController.send(
                                            "TOP_HOLO\n"
                                        )

                                    } else {

                                        bluetoothController.send(
                                            "TOP_HOLO_OFF\n"
                                        )
                                    }
                                }
                            )
                        }

                        // ======================================
                        // REAR HOLO
                        // ======================================

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(

                                text = "REAR HOLO",

                                color = Color.White,

                                fontSize = 16.sp,

                                fontWeight = FontWeight.Bold
                            )

                            Switch(

                                checked =
                                    rearHoloOn.value,

                                enabled =
                                    bluetoothController.isConnected,

                                onCheckedChange = { enabled ->

                                    rearHoloOn.value =
                                        enabled

                                    if (enabled) {

                                        bluetoothController.send(
                                            "REAR_HOLO\n"
                                        )

                                    } else {

                                        bluetoothController.send(
                                            "REAR_HOLO_OFF\n"
                                        )
                                    }
                                }
                            )
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    // ==========================================
                    // ON / OFF
                    // ==========================================

                    Button(

                        modifier =
                            Modifier.fillMaxWidth(),

                        enabled =
                            bluetoothController.isConnected,

                        colors =
                            ButtonDefaults.buttonColors(

                                containerColor =

                                    if (holoLightsEnabled.value)
                                        Color(0xFFD32F2F)
                                    else
                                        Color(0xFF00C853)
                            ),

                        onClick = {

                            holoLightsEnabled.value =
                                !holoLightsEnabled.value
                            topHoloOn.value = holoLightsEnabled.value
                            frontHoloOn.value = holoLightsEnabled.value
                            rearHoloOn.value = holoLightsEnabled.value


                            if (holoLightsEnabled.value) {

                                bluetoothController.send(
                                    "HOLO_LIGHTS_ON\n"
                                )

                            } else {

                                bluetoothController.send(
                                    "HOLO_LIGHTS_OFF\n"
                                )
                            }
                        }
                    ) {

                        Text(

                            text =

                                if (holoLightsEnabled.value)
                                    "TURN HOLOS OFF"
                                else
                                    "TURN HOLOS ON"
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    // ==========================================
                    // LEIA / RAINBOW
                    // ==========================================

                    Row(

                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp),

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Button(

                            modifier =
                                Modifier.weight(1f),

                            enabled =
                                bluetoothController.isConnected,

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Color(0xFF5E35B1)
                                ),

                            onClick = {

                                bluetoothController.send(
                                    "LEIA_MODE\n"
                                )
                            }
                        ) {

                            Text("LEIA")
                        }

                        Button(

                            modifier =
                                Modifier.weight(1f),

                            enabled =
                                bluetoothController.isConnected,

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Color(0xFF8E24AA)
                                ),

                            onClick = {

                                bluetoothController.send(
                                    "RAINBOW_MODE\n"
                                )
                            }
                        ) {

                            Text("RAINBOW")
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    // ==========================================
                    // TWITCH
                    // ==========================================

                    Row(

                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp),

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Button(

                            modifier =
                                Modifier.weight(1f),

                            enabled =
                                bluetoothController.isConnected,

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Color(0xFF00897B)
                                ),

                            onClick = {

                                bluetoothController.send(
                                    "TWITCH_ON\n"
                                )
                            }
                        ) {

                            Text("TWITCH ON")
                        }

                        Button(

                            modifier =
                                Modifier.weight(1f),

                            enabled =
                                bluetoothController.isConnected,

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Color(0xFF455A64)
                                ),

                            onClick = {

                                bluetoothController.send(
                                    "TWITCH_OFF\n"
                                )
                            }
                        ) {

                            Text("TWITCH OFF")
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )
                }
            }

            // ==================================================
            // REAR LOGIC DISPLAY
            // ==================================================

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(20.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color(0xFF101923)
                    ),

                border =
                    BorderStroke(
                        1.dp,
                        Color(0xFF284866)
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(

                        text = "REAR LOGIC DISPLAY",

                        color = Color(0xFF64C8FF),

                        fontSize = 18.sp,

                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    OutlinedTextField(

                        value =
                            rearLogicMessage.value,

                        onValueChange = {

                            rearLogicMessage.value = it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text(
                                "Display Message"
                            )
                        },

                        keyboardOptions =
                            KeyboardOptions(
                                capitalization =
                                    KeyboardCapitalization.Characters
                            ),

                        colors =
                            TextFieldDefaults.colors(

                                focusedContainerColor =
                                    Color(0xFF0E1724),

                                unfocusedContainerColor =
                                    Color(0xFF0E1724),

                                focusedTextColor =
                                    Color.White,

                                unfocusedTextColor =
                                    Color.White
                            )
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Button(

                        modifier =
                            Modifier.fillMaxWidth(),

                        enabled =
                            bluetoothController.isConnected,

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF00ACC1)
                            ),

                        onClick = {

                            bluetoothController.send(
                                "RLD:${rearLogicMessage.value}\n"
                            )
                        }
                    ) {

                        Text(
                            "SEND TO RLD"
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // ==================================================
            // TEST
            // ==================================================

            Button(

                enabled =
                    bluetoothController.isConnected,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),

                shape =
                    RoundedCornerShape(18.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF00ACC1)
                    ),

                onClick = {

                    bluetoothController.send(
                        "TEST\n"
                    )
                }
            ) {

                Text(

                    text = "RUN FULL DIAGNOSTICS",

                    fontSize = 18.sp,

                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(40.dp)
            )
        }
    }
}