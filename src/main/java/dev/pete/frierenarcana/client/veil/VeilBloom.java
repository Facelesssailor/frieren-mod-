package dev.pete.frierenarcana.client.veil;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.pete.frierenarcana.client.ArcanaShaders;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.renderer.RenderType;

/**
 * The bloom copy of the mod's glow. Only loaded with Veil. Vertices arrive through {@link Tee} alongside the normal
 * drawing; their alpha is scaled so the bright cores bloom and the large glass of a barrier barely does (the dome is
 * recognised by the colour tag the energy shader reads: green .90, blue .60-.655).
 */
final class VeilBloom {
    private static ByteBufferBuilder memory;
    private static BufferBuilder builder;

    private VeilBloom() {
    }

    static VertexConsumer tee(VertexConsumer main) {
        if (builder == null) {
            if (memory == null) {
                memory = new ByteBufferBuilder(1 << 18);
            }
            builder = new BufferBuilder(memory, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }
        return new Tee(main, builder);
    }

    static void flush() {
        BufferBuilder b = builder;
        builder = null;
        if (b == null) {
            return;
        }
        MeshData mesh = b.build();
        if (mesh != null) {
            Types.MAGIC.draw(mesh);
        }
    }

    static void discard() {
        if (builder != null) {
            MeshData mesh = builder.build();
            if (mesh != null) {
                mesh.close();
            }
            builder = null;
        }
    }

    /** How much of a vertex's opacity goes into the bloom copy. */
    static int bloomAlpha(int r, int g, int b, int a) {
        if (Math.abs(g - 230) <= 2 && b >= 149 && b <= 168) {
            // a barrier: at rest it does not bloom at all; struck, its bands and the last flashes glow a little
            return b >= 154 ? (int)(a * 0.22F) : 0;
        }
        return (int)(a * 0.6F);
    }

    static final class Tee implements VertexConsumer {
        private final VertexConsumer main;
        private final BufferBuilder bloom;

        Tee(VertexConsumer main, BufferBuilder bloom) {
            this.main = main;
            this.bloom = bloom;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.main.addVertex(x, y, z);
            this.bloom.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.main.setColor(r, g, b, a);
            this.bloom.setColor(r, g, b, bloomAlpha(r, g, b, a));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.main.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.main.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.main.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.main.setNormal(x, y, z);
            return this;
        }
    }

    /** The glow render type again, drawn additively into Veil's bloom buffer (which shares the world's depth). */
    static final class Types extends RenderType {
        static final RenderType MAGIC = create(
            "frieren_arcana_magic_bloom",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1 << 18,
            false,
            false,
            CompositeState.builder()
                .setShaderState(new ShaderStateShard(ArcanaShaders::energy))
                .setTransparencyState(LIGHTNING_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setOutputState(VeilRenderSystem.BLOOM_SHARD)
                .createCompositeState(false)
        );

        private Types(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling, boolean sort, Runnable setup, Runnable clear) {
            super(name, format, mode, size, crumbling, sort, setup, clear);
        }
    }
}
