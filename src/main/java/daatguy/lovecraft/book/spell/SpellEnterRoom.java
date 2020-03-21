package daatguy.lovecraft.book.spell;

import daatguy.lovecraft.core.LovecraftMain;
import daatguy.lovecraft.tileentity.TileEntityAltar;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpellEnterRoom extends Spell {
	
	public SpellEnterRoom() {
		this.obeliskLevel = TileEntityAltar.CHARGED_OBELISK;
		this.recipe = new ItemStack[9];
		for (int i = 0; i < 9; i++) {
			this.recipe[i] = new ItemStack(Blocks.GOLD_ORE);
		}
		this.name = "enter_room";
		this.color = 6;
	}
	
	@Override
	public boolean canCast(World world, BlockPos pos) {
		TileEntityAltar te = (TileEntityAltar)world.getTileEntity(pos);
		if (te==null || te.getObeliskType() != TileEntityAltar.CHARGED_OBELISK) {
			return false;
		} else {
			return super.canCast(world, pos);
		}
	}
	
	@Override
	public boolean castSpell(World world, BlockPos pos) {
		world.playSound(pos.getX(), pos.getY(), pos.getZ(),
				SoundEvents.ENTITY_GUARDIAN_AMBIENT, SoundCategory.NEUTRAL,
				1.0F, 1.2F, false);
		return true;
	}
	
}