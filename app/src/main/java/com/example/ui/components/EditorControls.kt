package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioOption
import com.example.data.model.QuoteCategory
import com.example.data.model.TypographyFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorControls(
    quoteText: String,
    authorText: String,
    brandHandle: String,
    category: QuoteCategory,
    aspectRatio: AspectRatioOption,
    textSize: Float,
    textAlignOption: String,
    showTagline: Boolean,
    brandPosition: String,
    typographyFamily: TypographyFamily,
    hasCustomPhoto: Boolean,
    overlayOpacity: Float,
    onQuoteChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onCategoryChange: (QuoteCategory) -> Unit,
    onAspectRatioChange: (AspectRatioOption) -> Unit,
    onTextSizeChange: (Float) -> Unit,
    onTextAlignChange: (String) -> Unit,
    onToggleTagline: (Boolean) -> Unit,
    onBrandPositionChange: (String) -> Unit,
    onTypographyChange: (TypographyFamily) -> Unit,
    onCustomPhotoSelected: (Uri?) -> Unit,
    onRemoveCustomPhoto: () -> Unit,
    onOverlayOpacityChange: (Float) -> Unit,
    onSuggestClick: () -> Unit,
    onInspireClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onCustomPhotoSelected(uri)
    }

    var categoryExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 1. Write Quote Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF151A21),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WRITE YOUR QUOTE",
                        color = Color(0xFF98A1AE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onInspireClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color(0xFF9BBCFF)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("inspire_quote_button")
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = "Inspire", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inspire", fontSize = 11.sp)
                        }

                        if (quoteText.isNotEmpty()) {
                            IconButton(
                                onClick = { onQuoteChange("") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF98A1AE))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quoteText,
                    onValueChange = onQuoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("quote_input_field"),
                    placeholder = { Text("Enter your quote here...", color = Color(0xFF64748B)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0B0E13),
                        unfocusedContainerColor = Color(0xFF0B0E13),
                        focusedBorderColor = Color(0xFF9BBCFF),
                        unfocusedBorderColor = Color(0xFF29313D),
                        focusedTextColor = Color(0xFFF5F6F8),
                        unfocusedTextColor = Color(0xFFF5F6F8)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "${quoteText.length} characters",
                    color = Color(0xFF98A1AE),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Author & Category Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Author", color = Color(0xFF98A1AE), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = authorText,
                            onValueChange = onAuthorChange,
                            placeholder = { Text("Author", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0B0E13),
                                unfocusedContainerColor = Color(0xFF0B0E13),
                                focusedBorderColor = Color(0xFF9BBCFF),
                                unfocusedBorderColor = Color(0xFF29313D),
                                focusedTextColor = Color(0xFFF5F6F8),
                                unfocusedTextColor = Color(0xFFF5F6F8)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category", color = Color(0xFF98A1AE), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = category.displayName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0B0E13),
                                    unfocusedContainerColor = Color(0xFF0B0E13),
                                    focusedBorderColor = Color(0xFF9BBCFF),
                                    unfocusedBorderColor = Color(0xFF29313D),
                                    focusedTextColor = Color(0xFFF5F6F8),
                                    unfocusedTextColor = Color(0xFFF5F6F8)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                modifier = Modifier.background(Color(0xFF151A21))
                            ) {
                                QuoteCategory.entries.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.displayName, color = Color(0xFFF5F6F8), fontSize = 13.sp) },
                                        onClick = {
                                            onCategoryChange(cat)
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Page / Brand handle
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Page / Brand Watermark", color = Color(0xFF98A1AE), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = brandHandle,
                        onValueChange = onBrandChange,
                        placeholder = { Text("@yourpage", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0B0E13),
                            unfocusedContainerColor = Color(0xFF0B0E13),
                            focusedBorderColor = Color(0xFF9BBCFF),
                            unfocusedBorderColor = Color(0xFF29313D),
                            focusedTextColor = Color(0xFFF5F6F8),
                            unfocusedTextColor = Color(0xFFF5F6F8)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Suggest Visual & Caption Button
                Button(
                    onClick = onSuggestClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9BBCFF),
                        contentColor = Color(0xFF07101B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("suggest_visual_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Suggest", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✨ Suggest Visual & Caption", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // 2. Format & Aspect Ratio Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF151A21),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "POST FORMAT & RATIO",
                    color = Color(0xFF98A1AE),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioOption.entries.forEach { option ->
                        val isSelected = aspectRatio == option
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0B0E13),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF9BBCFF) else Color(0xFF29313D)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onAspectRatioChange(option) }
                                .testTag("ratio_button_${option.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = option.label,
                                    color = if (isSelected) Color(0xFF9BBCFF) else Color(0xFFF5F6F8),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (option == AspectRatioOption.PORTRAIT) "Feed" else if (option == AspectRatioOption.SQUARE) "Square" else "Story",
                                    color = Color(0xFF98A1AE),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Photography & Overlay Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF151A21),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CUSTOM PHOTO & OVERLAY",
                    color = Color(0xFF98A1AE),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF181E27),
                            contentColor = Color(0xFFF5F6F8)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_photo_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Pick Photo", modifier = Modifier.size(16.dp), tint = Color(0xFF9BBCFF))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (hasCustomPhoto) "Change Photo" else "Upload Photo", fontSize = 12.sp)
                    }

                    if (hasCustomPhoto) {
                        Button(
                            onClick = onRemoveCustomPhoto,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A1515),
                                contentColor = Color(0xFFF87171)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7F1D1D)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Remove", fontSize = 12.sp)
                        }
                    }
                }

                if (hasCustomPhoto) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Overlay Dimming Level", color = Color(0xFF98A1AE), fontSize = 12.sp)
                        Text("${(overlayOpacity * 100).toInt()}%", color = Color(0xFF9BBCFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = overlayOpacity,
                        onValueChange = onOverlayOpacityChange,
                        valueRange = 0f..0.9f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF9BBCFF),
                            activeTrackColor = Color(0xFF9BBCFF),
                            inactiveTrackColor = Color(0xFF29313D)
                        )
                    )
                }
            }
        }

        // 4. Fine Tune Typography & Layout Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF151A21),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29313D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FINE TUNE DESIGN",
                    color = Color(0xFF98A1AE),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Font Family Row
                Text("Font Family", color = Color(0xFF98A1AE), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TypographyFamily.entries.forEach { family ->
                        val isSel = typographyFamily == family
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF1E293B) else Color(0xFF0B0E13),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) Color(0xFF9BBCFF) else Color(0xFF29313D)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onTypographyChange(family) }
                        ) {
                            Text(
                                text = family.label,
                                color = if (isSel) Color(0xFF9BBCFF) else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontFamily = family.fontFamily,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Text Size Scale", color = Color(0xFF98A1AE), fontSize = 12.sp)
                    Text("${textSize.toInt()} sp", color = Color(0xFF9BBCFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = textSize,
                    onValueChange = onTextSizeChange,
                    valueRange = 22f..64f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF9BBCFF),
                        activeTrackColor = Color(0xFF9BBCFF),
                        inactiveTrackColor = Color(0xFF29313D)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Text Alignment Buttons
                Text("Text Alignment", color = Color(0xFF98A1AE), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "auto" to "Template",
                        "left" to "Left",
                        "center" to "Center",
                        "right" to "Right"
                    ).forEach { (key, label) ->
                        val isSel = textAlignOption == key
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF1E293B) else Color(0xFF0B0E13),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) Color(0xFF9BBCFF) else Color(0xFF29313D)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onTextAlignChange(key) }
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color(0xFF9BBCFF) else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tagline Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Show Category Tagline", color = Color(0xFFF5F6F8), fontSize = 13.sp)
                        Text("e.g. \"PHILOSOPHY / 2026\" at top", color = Color(0xFF98A1AE), fontSize = 11.sp)
                    }
                    Switch(
                        checked = showTagline,
                        onCheckedChange = onToggleTagline,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF9BBCFF),
                            checkedTrackColor = Color(0xFF1E293B),
                            uncheckedThumbColor = Color(0xFF98A1AE),
                            uncheckedTrackColor = Color(0xFF0B0E13)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Brand Position Row
                Text("Watermark Position", color = Color(0xFF98A1AE), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "bottom" to "Bottom",
                        "top" to "Top",
                        "none" to "Hide"
                    ).forEach { (key, label) ->
                        val isSel = brandPosition == key
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF1E293B) else Color(0xFF0B0E13),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) Color(0xFF9BBCFF) else Color(0xFF29313D)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBrandPositionChange(key) }
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color(0xFF9BBCFF) else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
