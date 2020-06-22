package com.atomiccomics.crusoe.world;

import com.atomiccomics.crusoe.item.Item;

public record ItemPlaced(Item item, World.Coordinates location) {
}
