package daatguy.lovecraft.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemDecay extends ItemSimple {

	public int maxTicks;
	public ItemStack decaysInto;

	public ItemDecay(EnumRarity rarity, int maxTicks) {
		super(rarity);
		this.setMaxStackSize(1);
		this.maxTicks = maxTicks;
		this.decaysInto = ItemStack.EMPTY;
	}

	public ItemDecay(EnumRarity rarity, int maxTicks, ItemStack decaysInto) {
		super(rarity);
		this.setMaxStackSize(1);
		this.maxTicks = maxTicks;
		this.decaysInto = decaysInto;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		if (stack.hasTagCompound()
				&& stack.getTagCompound().hasKey("DecayTicks", 3))
			return true;
		return super.showDurabilityBar(stack);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn,
			int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		boolean updated = false;
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			updated = true;
		}
		if (!stack.getTagCompound().hasKey("LastUpdate", 4)) {
			stack.getTagCompound()
					.setLong("LastUpdate", worldIn.getWorldTime());
			updated = true;
		}
		if (!stack.getTagCompound().hasKey("DecayTicks", 3)) {
			stack.getTagCompound().setInteger("DecayTicks", 1);
			updated = true;
		}
		if (updated)
			return;
		NBTTagCompound nbt = stack.getTagCompound();
		nbt.setInteger("DecayTicks", nbt.getInteger("DecayTicks")
				+ (int) (worldIn.getWorldTime() - nbt.getLong("LastUpdate")));
		nbt.setLong("LastUpdate", worldIn.getWorldTime());
		if (nbt.getInteger("DecayTicks") > this.maxTicks
				&& entityIn instanceof EntityPlayer) {
			ItemStack newStack = this.decaysInto.copy();
			int count = newStack.getCount() * stack.getCount();
			if (count < newStack.getMaxStackSize()) {
				newStack.setCount(count);
				((EntityPlayer) entityIn).inventory.setInventorySlotContents(
						itemSlot, newStack);
			} else {
				// Sanity check for avoiding infinite loop
				if (newStack.getMaxStackSize() > 0) {
					newStack.setCount(newStack.getMaxStackSize());
					((EntityPlayer) entityIn).inventory
							.setInventorySlotContents(itemSlot, newStack.copy());
					count -= newStack.getMaxStackSize();
					while (count > newStack.getMaxStackSize()) {
						// If returns false, should summon item entity instead
						// Done in for loop because if you add at least 1 item
						// to an inventory addItemStack returns true,
						// Therefore it must be done 1 item at a time.
						for (int i = 0; i < newStack.getMaxStackSize(); i++) {
							ItemStack newStack1 = newStack.copy();
							newStack1.setCount(1);
							if (!((EntityPlayer) entityIn)
									.addItemStackToInventory(newStack1)) {
								summonItemInWorld(worldIn, entityIn,
										newStack1.copy());
							}
						}
						count -= newStack.getMaxStackSize();
					}
					// If returns false, should summon item entity instead
					// Done in for loop because if you add at least 1 item to an
					// inventory addItemStack returns true,
					// Therefore it must be done 1 item at a time.
					for (int i = 0; i < count; i++) {
						ItemStack newStack1 = newStack.copy();
						newStack1.setCount(1);
						if (!((EntityPlayer) entityIn)
								.addItemStackToInventory(newStack1.copy())) {
							summonItemInWorld(worldIn, entityIn,
									newStack1.copy());
						}
					}
				}

			}
		} else {
			stack.setTagCompound(nbt);
		}
	}

	protected void summonItemInWorld(World worldIn, Entity entityIn,
			ItemStack stackIn) {
		worldIn.spawnEntity(createEntity(worldIn, entityIn, stackIn));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack,
			ItemStack newStack, boolean slotChanged) {
		if (!super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged)) {
			return false;
		} else {
			// Messy as hell, this just returns false if both stacks have the
			// decay item NBT structure
			return !(oldStack.hasTagCompound()
					&& oldStack.getTagCompound().hasKey("DecayTicks", 3) && oldStack
					.getTagCompound().hasKey("LastUpdate", 4))
					&& !(newStack.hasTagCompound()
							&& newStack.getTagCompound()
									.hasKey("DecayTicks", 3) && newStack
							.getTagCompound().hasKey("LastUpdate", 4));
		}
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if (stack.hasTagCompound()
				&& stack.getTagCompound().hasKey("DecayTicks", 3)) {
			return ((double) stack.getTagCompound().getInteger("DecayTicks") / (double) this.maxTicks);
		} else {
			return super.getDurabilityForDisplay(stack);
		}
	}

	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return MathHelper.hsvToRGB(0.5f,
				(float) Math.pow(this.getDurabilityForDisplay(stack), 0.5d),
				1.0f - 0.5f * (float) this.getDurabilityForDisplay(stack));
	}

	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack) {
		itemstack = itemstack.copy();
		if (!itemstack.hasTagCompound()) {
			itemstack.setTagCompound(new NBTTagCompound());
		}
		if (!itemstack.getTagCompound().hasKey("LastUpdate", 4)) {
			itemstack.getTagCompound().setLong("LastUpdate",
					world.getWorldTime());
		}
		if (!itemstack.getTagCompound().hasKey("DecayTicks", 3)) {
			itemstack.getTagCompound().setInteger("DecayTicks", 1);
		}
		double d0 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
		double d1 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
		double d2 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
		EntityItem entityitem = new EntityItem(world, location.posX + d0,
				location.posY + d1, location.posZ + d2, itemstack);
		entityitem.setDefaultPickupDelay();
		return entityitem;
	};

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		boolean updated = false;
		ItemStack stack = entityItem.getItem();
		World worldIn = entityItem.world;
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			updated = true;
		}
		if (!stack.getTagCompound().hasKey("LastUpdate", 4)) {
			stack.getTagCompound()
					.setLong("LastUpdate", worldIn.getWorldTime());
			updated = true;
		}
		if (!stack.getTagCompound().hasKey("DecayTicks", 3)) {
			stack.getTagCompound().setInteger("DecayTicks", 1);
			updated = true;
		}
		if (updated)
			return false;
		NBTTagCompound nbt = stack.getTagCompound();
		nbt.setInteger("DecayTicks", nbt.getInteger("DecayTicks")
				+ (int) (worldIn.getWorldTime() - nbt.getLong("LastUpdate")));
		nbt.setLong("LastUpdate", worldIn.getWorldTime());
		if (nbt.getInteger("DecayTicks") > this.maxTicks) {
			entityItem.setItem(this.decaysInto.copy());
		}
		return super.onEntityItemUpdate(entityItem);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn,
			List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("DecayTicks", 3)) {
			int seconds = (this.maxTicks - stack.getTagCompound()
					.getInteger("DecayTicks")) / 20;
			tooltip.add("");
			if(seconds==1) {
				tooltip.add(TextFormatting.BLUE + I18n.format("tooltip.item_decay_single").replace(
						"*S",
						String.valueOf(seconds)));
			} else {
				tooltip.add(TextFormatting.BLUE + I18n.format("tooltip.item_decay").replace(
					"*S",
					String.valueOf(seconds)));
			}
		}
	}
}
