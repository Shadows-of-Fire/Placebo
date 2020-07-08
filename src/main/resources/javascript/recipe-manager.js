function initializeCoreMod() {
    return {
        'recipemanager': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.item.crafting.RecipeManager',
                'methodName': 'func_212853_a_',
                'methodDesc': '(Ljava/util/Map;Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/profiler/IProfiler;)V'
            },
            'transformer': function(method) {
                print('[PlaceboASM]: Patching RecipeManager#apply');

                var owner = "shadows/placebo/recipe/RecipeHelper";
                var name = "reload";
                var desc = "(Lnet/minecraft/item/crafting/RecipeManager;)V
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

				InsnList list = new InsnList();
				list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                var methodInsn = ASMAPI.buildMethodCall(
                    owner,
                    name,
                    desc,
                    ASMAPI.MethodType.STATIC);
				list.add(methodInsn);
                var node = instr.getLast();
                instr.insertBefore(node, list);

                return method;
            }
        }
    }
}