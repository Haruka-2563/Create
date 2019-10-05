package com.simibubi.create.modules.logistics.management.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock.Type;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class LogisticalControllerItem extends BlockItem {

	private Type type;

	public LogisticalControllerItem(Properties builder, Type type) {
		super(AllBlocks.LOGISTICAL_CONTROLLER.get(), builder);
		this.type = type;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			items.add(new ItemStack(this));
		}
	}

	public Type getType() {
		return type;
	}

}
