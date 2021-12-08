function initializeCoreMod() {
    return {
        'recipemanager': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.ServerResources',
                'methodName': '<init>',
                'methodDesc': '(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/command/Commands$CommandSelection;I)V'
            },
            'transformer': function(method) {
                var owner = "shadows/placebo/recipe/RecipeHelper";
                var name = "reload";
                var desc = "(Lnet/minecraft/world/item/crafting/RecipeManager;Lnet/minecraft/server/packs/resources/ReloadableResourceManager;)V";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
				ASMAPI.log('INFO', 'Patching DataPackRegistries#<init>');
				
				var i = 0;
				var list = new InsnList();
				list.add(new VarInsnNode(Opcodes.ALOAD, 0));
				list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/server/ServerResources", ASMAPI.mapField("f_136147_"), "Lnet/minecraft/world/item/crafting/RecipeManager;"));
				list.add(new VarInsnNode(Opcodes.ALOAD, 0));
				list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/server/ServerResources", ASMAPI.mapField("f_136145_"), "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;"));
                var methodInsn = ASMAPI.buildMethodCall(owner, name, desc, ASMAPI.MethodType.STATIC);
				list.add(methodInsn);
                var node;
				
				for(i = 0; i < instr.size(); i++){
					var ins = instr.get(i);
					if(ins.getOpcode() == Opcodes.INVOKESTATIC){
						node = ins;
						break;
					}	
				}
                instr.insert(node, list);

                return method;
            }
        }
    }
}