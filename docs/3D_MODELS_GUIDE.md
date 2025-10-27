# 3D Character Models Guide

## Overview

Your app now supports importing custom 3D character models to replace the basic geometric shapes. The system supports loading OBJ format models and seamlessly integrates with the existing animation system.

## Quick Start

### Option 1: Use Default Models
The app includes two built-in character models:
- `character.obj` - Simple block character
- `cartoon_character.obj` - More detailed cartoon-style character

These are automatically used when no custom model is specified.

### Option 2: Load Custom Models
Add your own 3D models to `app/src/main/assets/models/` and reference them in your UI:

```kotlin
WebGLCharacterView(
    animationState = CharacterAnimationState.THINKING,
    modelFilename = "my_character.obj" // Your custom model
)
```

## Creating Custom Models

### Supported Formats

Currently supports:
- ✅ **OBJ files** (.obj) - Recommended
- 🔜 GLTF support coming soon

### Model Requirements

For best results, your OBJ models should include:

1. **Vertices** - Define the 3D position of points
   ```
   v -0.5 0.5 0.5    # x y z coordinates
   ```

2. **Texture Coordinates** - Optional but recommended
   ```
   vt 0.0 0.0        # u v coordinates
   ```

3. **Normals** - For proper lighting (automatically calculated if missing)
   ```
   vn 0.0 1.0 0.0    # x y z normal vector
   ```

4. **Faces** - Define how vertices form triangles
   ```
   f 1/1/1 2/2/1 3/3/1    # v/vt/vn format
   ```

### Getting Started with Models

#### Option A: Create Models from Scratch

Tools to create models:
- **Blender** (Free, Professional) - [blender.org](https://www.blender.org)
- **Tinkercad** (Free, Online) - [tinkercad.com](https://www.tinkercad.com)
- **SketchUp** (Free/Premium) - [sketchup.com](https://www.sketchup.com)

#### Option B: Download Models

Free 3D model sources:
- **Sketchfab** - [sketchfab.com](https://sketchfab.com) - Filter by "Download" and "OBJ format"
- **Free3D** - [free3d.com](https://free3d.com)
- **TurboSquid** - [turbosquid.com](https://www.turbosquid.com) - Search for "free"

**Important**: Ensure models are:
- ✅ Optimized for mobile (low polygon count)
- ✅ Exported as OBJ format
- ✅ Properly scaled (recommended size: 0.5-2.0 units)
- ✅ Have UV coordinates for textures

## Usage in Code

### Basic Usage
```kotlin
// In your Compose screen
WebGLCharacterView(
    animationState = CharacterAnimationState.THINKING,
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp),
    onCharacterReady = {
        // Called when character is loaded and ready
    },
    modelFilename = "my_character.obj" // Custom model name
)
```

### Loading Models from Assets

1. **Place your model file** in `app/src/main/assets/models/`

2. **Reference in code**:
   ```kotlin
   val characterView = WebGLCharacterView(
       animationState = CharacterAnimationState.HAPPY,
       modelFilename = "robot.obj"
   )
   ```

3. **That's it!** The model will automatically load and use existing animations.

## Animation States

Your custom models will automatically respond to these states:

```kotlin
CharacterAnimationState.IDLE           // Gentle floating
CharacterAnimationState.THINKING      // Slow thinking motion
CharacterAnimationState.HAPPY         // Bouncy happy animation
CharacterAnimationState.SAD            // Droopy, slow motion
CharacterAnimationState.CELEBRATING   // Energetic celebration
CharacterAnimationState.ENCOURAGING    // Supportive motion
CharacterAnimationState.CORRECT_ANSWER // Quick celebration
CharacterAnimationState.WRONG_ANSWER   // Shake animation
```

### Using Different Animations

```kotlin
var animationState by remember { mutableStateOf(CharacterAnimationState.IDLE) }

// Change animation based on quiz state
if (correct) {
    animationState = CharacterAnimationState.CORRECT_ANSWER
} else {
    animationState = CharacterAnimationState.WRONG_ANSWER
}

WebGLCharacterView(
    animationState = animationState,
    modelFilename = "student.obj"
)
```

## Model Optimization Tips

### For Best Performance

1. **Keep polygon count low**
   - Mobile-friendly: 500-2000 triangles
   - Complex scenes: 2000-5000 triangles
   - Target: Under 1000 for character animations

2. **Optimize vertex data**
   - Remove unnecessary vertices
   - Use triangle strips when possible
   - Minimize duplicate vertices

3. **Texture size**
   - Use power-of-2 sizes: 256x256, 512x512, 1024x1024
   - Mobile: 512x512 is usually sufficient
   - Keep under 1024x1024 for animations

4. **File size**
   - Target: Under 100KB for character models
   - Compress where possible
   - Remove unnecessary details

### Example: Creating a Quiz Character in Blender

1. **Create your character** (keep it simple for mobile)
2. **Export as OBJ**:
   - File → Export → Wavefront (.obj)
   - Check: "Triangulate Faces"
   - Check: "Export UV"
   - Check: "Export Normals"
3. **Optimize**:
   - Use "Decimate" modifier for lower polygon count
   - Keep under 1000 vertices for smooth 60 FPS
4. **Place file** in `assets/models/` folder

## Troubleshooting

### Model not loading

**Check**:
- File is in `app/src/main/assets/models/` folder
- File extension is `.obj`
- Filename is spelled correctly (case-sensitive)

**Debug**:
```kotlin
// Add logging to see what's happening
WebGLCharacterView(
    onCharacterReady = {
        Log.d("Model", "Character loaded successfully!")
    },
    modelFilename = "my_model.obj"
)
```

### Model appears too large/small

The renderer automatically scales models. If your model appears incorrect:

**Adjust in code**:
```kotlin
// In CharacterModel.kt, modify the scale factor
private var characterScale = 0.8f  // Adjust this value
```

### Performance issues

**Reduce complexity**:
1. Lower polygon count (< 1000 triangles)
2. Simplify textures
3. Remove unnecessary geometry
4. Use the performance manager automatically built-in

**Enable low-quality mode**:
```kotlin
// Already handled automatically for low-end devices!
// The app detects device capabilities
```

## Advanced Usage

### Loading Multiple Models

```kotlin
// In a quiz with different characters per question
var currentModel by remember { 
    mutableStateOf("cartoon_character.obj") 
}

WebGLCharacterView(
    animationState = animationState,
    modelFilename = currentModel
)

// Change model mid-quiz
currentModel = "robot.obj"
```

### Custom Animations

You can modify animation properties in `CharacterModel.kt`:

```kotlin
fun updateAnimation(animationState: CharacterAnimationState, time: Float) {
    when (animationState) {
        CharacterAnimationState.HAPPY -> {
            headRotation = (Math.sin(time.toDouble() * 3) * 20).toFloat()
            // Add your custom animation logic
        }
        // ... other states
    }
}
```

## Resources

### Recommended Model Sources

1. **Blender Characters** (Free)
   - Search "low poly character" on Sketchfab
   - Download Blender files, export as OBJ

2. **Ready-made OBJ Characters**
   - [Poly Haven](https://polyhaven.com)
   - [CGTrader Free](https://www.cgtrader.com/free-3d-models)

3. **Create Your Own**
   - Use Blender's basic mesh tools
   - Start with a simple cube
   - Subdivide and sculpt

### Technical Resources

- **OBJ Format Spec**: [Wikipedia](https://en.wikipedia.org/wiki/Wavefront_.obj_file)
- **Blender Tutorials**: [Blender.org](https://www.blender.org/support/tutorials/)
- **Mobile 3D Optimization**: [Android Developer Guide](https://developer.android.com/training/graphics/opengl)

## Example: Complete Workflow

### Step 1: Find/Create Model
```bash
# Download a free character model
# Place it in: app/src/main/assets/models/kid_character.obj
```

### Step 2: Use in Code
```kotlin
@Composable
fun MyQuizScreen() {
    var animState by remember { 
        mutableStateOf(CharacterAnimationState.THINKING) 
    }
    
    Column {
        // Your custom 3D character
        WebGLCharacterView(
            animationState = animState,
            modelFilename = "kid_character.obj",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
        
        // Quiz content below...
    }
}
```

### Step 3: Animate Based on Quiz State
```kotlin
// When user selects answer
if (isCorrect) {
    animState = CharacterAnimationState.CELEBRATING
} else {
    animState = CharacterAnimationState.ENCOURAGING
}
```

## Next Steps

✅ Your app now supports custom 3D models!
✅ Models work with all existing animations
✅ Automatic optimization for device performance
✅ Fallback to built-in models if loading fails

**Try these improvements**:
1. Find a character model online
2. Export it as OBJ
3. Place in `assets/models/` folder
4. Reference in your quiz screens
5. Enjoy your animated character!

---

**Questions?** Check the code in:
- `ModelLoader.kt` - Model loading logic
- `CharacterModel.kt` - Animation system
- `EnhancedWebGLRenderer.kt` - Rendering engine

