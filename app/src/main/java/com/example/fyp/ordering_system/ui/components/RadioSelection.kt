package com.example.fyp.ordering_system.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fyp.menucreator.data.model.ModifierItem

@Composable
fun RadioSelection(
    items : List<ModifierItem>,
    selectedItem: ModifierItem?,
    onClick: (ModifierItem) -> Unit
) {
    println("rs $selectedItem")
    Column {
        items.forEach {item ->
            if (item.availability){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = item == selectedItem,
                            onClick = { onClick(item) }
                        )
                        .padding(horizontal = 8.dp)
                        .align(Alignment.Start),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = item == selectedItem,
                        onClick = {
                            onClick(item)
                        }
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
}

@Preview(showBackground = true)
@Composable
fun RadioSelectionPreview(){
    RadioSelection(items = listOf(
        ModifierItem(name = "ABC", price = 2.0),
        ModifierItem(name = "HAdcsD", price = 2.0),
        ModifierItem(name = "AKNvsdsfvsfkjcsddajbudfciouhsicahawikbhibvSUbiushaujabGS<JbAsb", price = 2.0),
    ), selectedItem = null, onClick = {})
}