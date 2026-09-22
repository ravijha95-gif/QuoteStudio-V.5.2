package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.generator.TemplateSuggestion
import com.example.data.model.PostTemplate
import com.example.data.model.TemplateGroup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemplateSelector(
    selectedTemplateId: String,
    currentFilter: TemplateGroup,
    suggestions: List<TemplateSuggestion>,
    onFilterChange: (TemplateGroup) -> Unit,
    onTemplateSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TemplateGroup.entries.forEach { group ->
                val isSelected = currentFilter == group
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF12161D),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF9BBCFF) else Color(0xFF29313D)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onFilterChange(group) }
                        .testTag("filter_chip_${group.name.lowercase()}")
                ) {
                    Text(
                        text = group.label,
                        color = if (isSelected) Color(0xFF9BBCFF) else Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 2. Semantic AI Recommendations (if any)
        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "RECOMMENDED FOR THIS QUOTE",
                color = Color(0xFF98A1AE),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            suggestions.take(3).forEachIndexed { index, suggestion ->
                val isSelected = suggestion.template.id == selectedTemplateId
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF152033) else Color(0xFF11151B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF9BBCFF) else Color(0xFF29313D)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTemplateSelect(suggestion.template.id) }
                        .testTag("suggestion_item_${suggestion.template.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (index == 0) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF9BBCFF).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✨", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = suggestion.template.name,
                                    color = if (isSelected) Color(0xFF9BBCFF) else Color(0xFFF5F6F8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (index == 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF9BBCFF).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "BEST MATCH",
                                            color = Color(0xFF9BBCFF),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = suggestion.template.description,
                                color = Color(0xFF98A1AE),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF9BBCFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Grid of Templates
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "PRE-EXISTING TEMPLATES",
            color = Color(0xFF98A1AE),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        val filteredTemplates = when (currentFilter) {
            TemplateGroup.ALL -> PostTemplate.ALL_TEMPLATES
            TemplateGroup.MINIMAL -> PostTemplate.ALL_TEMPLATES.filter { it.group == TemplateGroup.MINIMAL }
            TemplateGroup.PHOTOGRAPHY -> PostTemplate.ALL_TEMPLATES.filter { it.group == TemplateGroup.PHOTOGRAPHY }
            TemplateGroup.EDITORIAL -> PostTemplate.ALL_TEMPLATES.filter { it.group == TemplateGroup.EDITORIAL }
            TemplateGroup.CINEMATIC -> PostTemplate.ALL_TEMPLATES.filter { it.group == TemplateGroup.CINEMATIC }
            TemplateGroup.PHRASE -> PostTemplate.ALL_TEMPLATES.filter { it.group == TemplateGroup.PHRASE }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filteredTemplates.forEach { template ->
                val isSel = template.id == selectedTemplateId
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0C0F14),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSel) 2.dp else 1.dp,
                        if (isSel) Color(0xFF9BBCFF) else Color(0xFF29313D)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTemplateSelect(template.id) }
                        .testTag("template_card_${template.id}")
                ) {
                    Column {
                        // Mini sample box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.2f)
                                .background(template.primaryBgColor)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = template.sampleText,
                                color = template.textColor,
                                fontSize = 11.sp,
                                fontFamily = template.defaultFontFamily.fontFamily,
                                fontWeight = template.defaultFontWeight,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }

                        // Label
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = template.name,
                                color = if (isSel) Color(0xFF9BBCFF) else Color(0xFFE6E9ED),
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                            if (isSel) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF9BBCFF))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
