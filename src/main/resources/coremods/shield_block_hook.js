function initializeCoreMod() {
    return {
        'placeboshieldblock': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.LivingEntity',
                'methodName': 'func_70097_a',
                'methodDesc': '(Lnet/minecraft/util/DamageSource;F)Z'
            },
            'transformer': function(method) {
                var owner = "shadows/placebo/events/PlaceboEventHooks";
                var name = "onShieldBlock";
                var desc = "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/DamageSource;F)F";
                var instr = method.instructions;

                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
				var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
				ASMAPI.log('INFO', 'Patching LivingEntity#attackEntityFrom');

				var i;
				var j = 0;
				for (i = 0; i < instr.size(); i++) {
					var n = instr.get(i);
					if (n.getOpcode() == Opcodes.ISTORE && n.var == 4) {
						if (++j == 2) { //We want the second ISTORE 4
							var insn = new InsnList();
							insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
							insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
							insn.add(new VarInsnNode(Opcodes.FLOAD, 5));
							insn.add(ASMAPI.buildMethodCall(
								owner,
								name,
								desc,
								ASMAPI.MethodType.STATIC));
							insn.add(new VarInsnNode(Opcodes.FLOAD,  5));	//Load amtBlocked (which was amount)
							insn.add(new VarInsnNode(Opcodes.FSTORE, 2));	//amount = amtBlocked
							insn.add(new VarInsnNode(Opcodes.FSTORE, 5));	//amtBlocked = eventBlocked
							insn.add(new VarInsnNode(Opcodes.FLOAD,  2));	//Load amount
							insn.add(new VarInsnNode(Opcodes.FLOAD,  5));	//Load curBlocked
							insn.add(new InsnNode(Opcodes.FSUB));			//amount - curBlocked
							insn.add(new VarInsnNode(Opcodes.FSTORE, 2));	//amount = amount - curBlocked
							instr.insert(n, insn);
						}
					}
				}

                return method;
            }
        }
    }
}