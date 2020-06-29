# World

The World aggregate is responsible for the physical placement of other entities.
This includes the player, walls, and items.

The world has a fixed set of dimensions, and does not wrap.
Items are not allowed to travel beyond the world bounds.
If the world is ever resized during play, the player is relocated to the closest spot within the new bounds if they are smaller.
Any items or walls extending beyond the new bounds are simply destroyed.