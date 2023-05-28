package com.example.fyp.ordering_system.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fyp.menucreator.data.model.ModifierItem


@Composable
fun CheckboxSelection(
    items: List<ModifierItem>,
    selectedItems : SnapshotStateList<ModifierItem>,
    onClick : (List<ModifierItem>) -> Unit
){
    items.forEach { item ->
        if (item.availability) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clickable {
                        if (!selectedItems.contains(item)) {
                            selectedItems.add(item)
                        } else {
                            selectedItems.remove(item)
                        }
                        onClick(selectedItems)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedItems.contains(item),
                    onCheckedChange = {
                        if (it){
                            selectedItems.add(item)
                        } else
                            selectedItems.remove(item)
                        onClick(selectedItems)
                    },
                    enabled = true
                )
                Text(
                    text = item.name,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(4f)
                )
                Text(
                    text = item.price.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}