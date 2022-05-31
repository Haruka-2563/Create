package com.simibubi.create.foundation.mixin;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationMapData;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationMarker;
import com.simibubi.create.content.logistics.trains.management.edgePoint.station.StationTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin implements StationMapData {

	@Final
	@Shadow
	public int x;

	@Final
	@Shadow
	public int z;

	@Final
	@Shadow
	public byte scale;

	private final Map<String, StationMarker> stationMarkers = Maps.newHashMap();

	@Inject(
			method = "save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;",
			at = @At("RETURN")
	)
	public void save(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {

		ListTag listTag = new ListTag();
		for (StationMarker stationMarker : this.stationMarkers.values()) {
			listTag.add(stationMarker.save());
		}

		cir.getReturnValue().put("create:stations", listTag);
	}

	@Inject(
			method = "load(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;",
			at = @At("RETURN")
	)
	private static void load(CompoundTag compound, CallbackInfoReturnable<MapItemSavedData> cir) {
		MapItemSavedData mapData = cir.getReturnValue();
		StationMapData stationMapData = (StationMapData) mapData;

		ListTag listTag = compound.getList("create:stations", 10);
		for (int k = 0; k < listTag.size(); ++k) {
			StationMarker stationMarker = StationMarker.load(listTag.getCompound(k));
			stationMapData.loadStationMarker(stationMarker);
		}
	}

	@Override
	public void loadStationMarker(StationMarker marker) {
		stationMarkers.put(marker.getId(), marker);
		addDecoration(marker.getType(), null, marker.getId(), marker.getPos().getX(), marker.getPos().getZ(), 180.0D, marker.getName());
	}

	@Shadow
	private void addDecoration(MapDecoration.Type pType, @Nullable LevelAccessor pLevel, String pDecorationName, double pLevelX, double pLevelZ, double pRotation, @Nullable Component pName) {
		throw new AssertionError();
	}

	@Shadow
	private void removeDecoration(String pIdentifier) {
		throw new AssertionError();
	}

	@Shadow
	public boolean isTrackedCountOverLimit(int pTrackedCount) {
		throw new AssertionError();
	}

	@Override
	public boolean toggleStation(LevelAccessor level, BlockPos pos, StationTileEntity stationTileEntity) {
		double xCenter = pos.getX() + 0.5D;
		double zCenter = pos.getZ() + 0.5D;
		int scaleMultiplier = 1 << this.scale;

		double localX = (xCenter - (double) this.x) / (double) scaleMultiplier;
		double localZ = (zCenter - (double) this.z) / (double) scaleMultiplier;

		if (localX < -63.0D || localX > 63.0D || localZ < -63.0D || localZ > 63.0D)
			return false;

		StationMarker marker = StationMarker.fromWorld(level, pos);
		if (marker == null)
			return false;

		if (this.stationMarkers.remove(marker.getId(), marker)) {
			this.removeDecoration(marker.getId());
			return true;
		}

		if (!this.isTrackedCountOverLimit(256)) {
			this.stationMarkers.put(marker.getId(), marker);
			this.addDecoration(marker.getType(), level, marker.getId(), xCenter, zCenter, 180.0D, marker.getName());
		}

		return false;
	}

	@Inject(
			method = "checkBanners(Lnet/minecraft/world/level/BlockGetter;II)V",
			at = @At("RETURN")
	)
	public void checkBanners(BlockGetter pReader, int pX, int pZ, CallbackInfo ci) {
		Iterator<StationMarker> iterator = this.stationMarkers.values().iterator();

		while (iterator.hasNext()) {
			StationMarker marker = iterator.next();
			if (marker.getPos().getX() == pX && marker.getPos().getZ() == pZ) {
				StationMarker other = StationMarker.fromWorld(pReader, marker.getPos());
				if (!marker.equals(other)) {
					iterator.remove();
					this.removeDecoration(marker.getId());
				}
			}
		}
	}
}
