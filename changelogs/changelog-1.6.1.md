# 1.6.1

- Fixed Portal Distortion not applying after crossing a Nether portal. You arrive standing inside the destination portal, but Minecraft ignores that portal until its cooldown runs out, so with a Nausea effect active the distortion switched to the Nausea percentage while you were still in the portal. Stepping out and back in was the only way to get the portal percentage back. Touching a portal now always counts as the portal, cooldown or not
- Corrected the 1.6.0 notes about the green nausea texture. It is not left to vanilla: Nausea Distortion drives it the same way Minecraft's own Distortion Effects slider does, so lowering Nausea Distortion trades the spinning for the green cue, and at 0% there is no spinning and the texture is at full strength. While a portal is the source, the texture follows Minecraft's slider only. The code always behaved this way; only the notes were wrong

## Internal

- New `EntityMixin` records contact with a Nether portal at the head of `setAsInsidePortal`, before vanilla's cooldown check, and `LocalPlayerMixin` counts that contact as the portal when attributing the distortion source
