@file:OptIn(ExperimentalMaterial3Api::class)

package net.nitakan.githubsearcher.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotFoundRepositoryCard(modifier: Modifier, onClick: (() -> Unit)? = null) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = "指定されたリポジトリは存在しません")
            Spacer(modifier = Modifier.size(8.dp))
            if (onClick != null) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("再試行する")
                }
            }
        }
    }
}