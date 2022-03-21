package com.setana.treenity.data.api.dto.mypage.tree

data class MyTreeItem (
    val treeId: Int,
    val treeName: String,
    val item: Item,
    val bucket: Int,
    val level: Int,
    val createdDate: String
)