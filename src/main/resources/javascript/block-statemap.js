function initializeCoreMod() {
    return {
        'blockstatemap': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.BlockModelShapes',
                'methodName': 'func_209553_a',
                'methodDesc': '(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/renderer/model/ModelResourceLocation;'
            },
            'transformer': function(method) {
                print('[PlaceboASM]: Patching BlockModelShapes#getModelLocation');

                var owner = "shadows/placebo/statemap/ModelMapRegistry";
                var name = "getMRL";
                var desc = "(Lnet/minecraft/block/BlockState;Lnet/minecraft/client/renderer/model/ModelResourceLocation;)Lnet/minecraft/client/renderer/model/ModelResourceLocation;";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

				instr.insert(new VarInsnNode(Opcodes.ALOAD, 1));
                var methodInsn = ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC);
                var node = instr.getLast().getPrevious();
                instr.insertBefore(node, methodInsn);

                return method;
            }
        }
    }
}