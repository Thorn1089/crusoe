package com.atomiccomics.crusoe.world;

import com.atomiccomics.crusoe.item.Item;

public record ItemRemoved(Item item, World.Coordinates location) {
}
