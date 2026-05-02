# Painter

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21-brightgreen)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.0.167-blue)](https://neoforged.net/)
[![Mod Version](https://img.shields.io/badge/Version-2.8.7-orange)](https://github.com/SouthernWall_404/Painter/releases)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)](LICENSE)

Painter is an advanced decorative mod designed for Minecraft 1.21 NeoForge platform, providing powerful wall painting, texture customization, and area selection features. Through an intuitive tool system, players can easily paint any block surface with the appearance of other blocks, enabling unlimited creative architectural decoration.

## 📋 Features

### 🎨 Core Tools

#### Paint Brush
- **Basic Painting** - Hold target block in off-hand, right-click with brush to paint block surfaces
- **Multi-Direction Support** - Independent painting for all six directions (E, W, S, N, Up, Down)
- **Texture Cycling** - Cycle through UV offsets for texture tiling effects
- **Property Toggle** - Switch block orientation, axis, open/close states

#### Chulk
- **Precise Selection** - Two-point corner selection system
- **Wall Recognition** - Automatically identifies and highlights valid paintable walls
- **Sneak Clear** - Sneak + mouse click to quickly clear selection
- **Visual Feedback** - Real-time rendering of selection boundaries and paintable faces

#### Paint Bucket
- **Large Area Fill** - Batch spray painting by wall units
- **Selection Integration** - Works with chulk selection for precise control
- **Efficient Operation** - Complete entire wall painting in one action

### 🛠️ Advanced Features

#### Smart Rendering System
- **Frustum Culling** - Automatically hides invisible faces for performance
- **Ambient Occlusion (AO)** - Realistic lighting calculations
- **Dynamic Refresh** - Auto-refreshes visibility and lighting on chunk load
- **Cache Optimization** - BakedQuad caching reduces redundant calculations

#### Data Persistence
- **Attachment System** - Stores paint data using NeoForge Attachment API
- **Chunk Indexing** - PaintChunkInfo for quick location of painted chunks
- **NBT Serialization** - Complete save/load mechanism
- **Network Sync** - Automatic server-client data synchronization

#### Compatibility
- **Full Blocks** - Supports all standard blocks with full collision boxes
- **Slab Blocks** - Supports top, bottom, and double slabs
- **Directional Blocks** - Supports stairs, trapdoors, and other directional blocks
- **Extensible Architecture** - AbstractPaint-based design for easy extension

## 📦 Installation

### Prerequisites
- **Minecraft**: 1.21
- **NeoForge**: 21.0.167 or higher
- **Laplace API**: 0.5.1 (Required dependency)

> ⚠️ **Important**: Laplace API is a core dependency providing math operations, rendering engine, and network sync.

### Installation Steps
1. Install the corresponding version of NeoForge
2. Download Painter mod file (.jar)
3. Place the mod file in Minecraft's `mods` folder
4. Launch the game and enjoy!

## 🎮 Usage Guide

### Paint Brush - Basic Painting

1. **Preparation**
   - Place desired texture block in off-hand slot
   - Hold paint brush in main hand

2. **Execute Painting**
   - Right-click any face of target block
   - The face will be painted with off-hand block texture

3. **Advanced Operations**
   - **Cycle Texture**: Right-click painted face again to cycle UV offset
   - **Toggle Direction**: For directional blocks (stairs), right-click to rotate
   - **Remove Paint**: Use empty hand or specific tool to restore original state

### Chulk - Area Selection

1. **Set Selection Start**
   - Left-click first corner block
   - Screen shows selection preview

2. **Set Selection End**
   - Right-click diagonal opposite corner
   - All valid walls between points are marked

3. **View Selection**
   - Selection boundaries display as highlighted lines
   - Paintable faces have special markers

4. **Clear Selection**
   - Press Sneak (Shift) + any mouse button
   - Or select new start point

5. **Use with Paint Bucket**
   - Use paint bucket within selection for batch painting
   - Only affects valid walls within selection

### Paint Bucket - Batch Painting

1. **Basic Usage**
   - Place target texture block in off-hand
   - Hold paint bucket in main hand
   - Right-click wall for large-area painting

2. **Selection Mode**
   - Define selection with chulk first
   - Use paint bucket within selection
   - Automatically fills all paintable walls

3. **Notes**
   - Paint bucket operates by "wall" units
   - Continuous same-direction planes treated as one wall
   - Ideal for quick decoration of large buildings

## ⚙️ Configuration

Painter provides client and server configuration files located at `.minecraft/config/painter-*.toml`.

### Client Config (painter-client.toml)

```toml
# Paint layer offset, controls distance between paint texture and original block
# Default: 0.005
# Range: 0.001 - 0.05
paint_offset = 0.005

# Selection border color (RGB)
# Default: #00FF00 (Green)
selection_color = "#00FF00"

# Enable selection rendering
enable_selection_render = true
```

### Server Config (painter-common.toml)

```toml
# Maximum painting distance (blocks)
# Prevents remote operation abuse
max_paint_distance = 64

# Allow paint tools in protected areas
allow_in_protected_areas = false
```

> 💡 **Tip**: Game restart required for config changes to take effect.

## 🔧 Development Setup

### Prerequisites
- **JDK**: Java 21 or higher
- **IDE**: IntelliJ IDEA (recommended) or Eclipse
- **Gradle**: Included Gradle Wrapper
- **Git**: Version control system

### Quick Start

```bash
# 1. Clone repository
git clone https://github.com/SouthernWall_404/Painter.git
cd Painter

# 2. Build mod
./gradlew build

# 3. Run test environments
./gradlew runClient              # Launch client
./gradlew runServer              # Launch server
./gradlew runGameTestServer      # Run game tests

# 4. Generate data resources
./gradlew runData                # Run data generator
```

### IDE Configuration

#### IntelliJ IDEA
1. Open project root directory
2. Wait for Gradle auto-sync (or manually click "Reload All Gradle Projects")
3. If dependency issues occur, run: `./gradlew --refresh-dependencies`
4. Ensure JDK 21 is properly configured

#### Eclipse
1. Run `./gradlew eclipse` to generate Eclipse config files
2. Import project into Eclipse
3. Refresh project to ensure dependencies load correctly

## 🐛 Known Issues

### Chunk Load Delay Rendering

**Issue**: Paint effects may not display immediately when reloading previously visited areas.

**Workaround**:
- Place or break a block nearby to trigger chunk update
- Or use light source to refresh lighting

**Status**: ✅ Fixed with visibility refresh mechanism, but may still occur in extreme cases

### Performance Considerations

- **Large Paint Counts**: Hundreds of paint blocks in single chunk may affect FPS
- **Recommendation**: Decorate large buildings in sections

### Compatibility Limitations

- **Unsupported Block Types**:
  - Fluid blocks (water, lava)
  - Plant blocks (flowers, grass)
  - Entity blocks (paintings, item frames)
  - Redstone components (some may display abnormally)

## 🤝 Contributing

We welcome all forms of contributions! Bug reports, feature suggestions, and code contributions.

### Code Contribution Flow

1. **Fork this repository**
   ```bash
   git fork https://github.com/SouthernWall_404/Painter.git
   ```

2. **Create feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
   ```

3. **Develop and test**
   - Follow existing code style
   - Add necessary comments
   - Ensure no new warnings introduced

4. **Commit changes**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   # or
   git commit -m "fix: resolve bug description"
   ```

5. **Push to branch**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Open Pull Request**
   - Describe your changes in detail
   - Explain testing method
   - Link related Issues

### Code Standards

- **Naming**: camelCase (variables/methods) and PascalCase (class names)
- **Comments**: Add Javadoc for public APIs
- **Formatting**: Use IDE default Java formatting rules
- **Imports**: Organize imports alphabetically

### Issue Report Template

When reporting bugs, include:
- Minecraft version
- NeoForge version
- Painter version
- Laplace API version
- Detailed reproduction steps
- Expected vs actual behavior
- Crash report (if any)

## 📄 License

This project uses **All Rights Reserved** license.

- ❌ No copying, modification, distribution, or use without explicit written permission
- ❌ No commercial use of this mod
- ❌ No creation and distribution of derivative works
- ✅ Personal non-commercial use is permitted

See [LICENSE](LICENSE) file for detailed terms.

## 🔗 Related Links

### Official Resources
- [NeoForge Documentation](https://docs.neoforged.net/)
- [NeoForge Discord Community](https://discord.neoforged.net/)
- [Minecraft Official Website](https://www.minecraft.net/)

### Project Dependencies
- [Laplace API](https://github.com/SouthernWall_404/LaplaceAPI) - Core prerequisite library
  - Math operations library (Math37)
  - Rendering engine (RegulappleEngine)
  - Attachment system (VertinCore)
  - Network synchronization (xNetwork)

### Community
- [GitHub Issues](https://github.com/SouthernWall_404/Painter/issues) - Bug reports
- [GitHub Releases](https://github.com/SouthernWall_404/Painter/releases) - Version downloads

## 📞 Contact

For questions, suggestions, or collaboration opportunities:

- 📧 **GitHub Issues**: [Submit Issue](https://github.com/SouthernWall_404/Painter/issues)
- 💬 **Discord**: SouthernWall_404#0000
- 🎮 **Minecraft Community Forums**

## 🙏 Acknowledgments

Thanks to the following open source projects and communities:

- **NeoForge Team** - For providing powerful mod development platform
- **Mojang Studios** - For Minecraft game
- **Laplace API** - For providing base framework support
- **All Contributors** - Thanks to every user who submitted Issues and PRs

---

<div align="center">

**Painter** - Make your architectural creations more colorful! 🎨

*Made with ❤️ by SouthernWall_404*

</div>
