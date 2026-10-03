# CS Weapons (Fabric, Minecraft 1.21.4)

Build:  gradle build      (JDK 21 + Gradle 8.12, or copy src/ + build files into the official
                           template from https://fabricmc.net/develop/template/)
Result: build/libs/csweapons-1.0.0.jar -> .minecraft/mods (needs Fabric Loader + Fabric API)
Dev run: gradle runClient

Controls
  Right-click  fire            R  reload (rebind under Controls > CS Weapons)
  Shift (AWP)  scope in - the AWP is only accurate while scoped

Magazines: each gun holds a fixed number of rounds (bar under the icon + HUD bottom-right).
Reloading takes bullets from your inventory when it finishes; switching weapons cancels it.
An empty gun auto-reloads on click. Creative mode has unlimited reserve ammo.
