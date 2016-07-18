/*
 * Copyright (C) 2016 Tencent WeChat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tinker.commons.dexdifflib.util;

import com.tencent.tinker.android.dex.DexException;
import com.tencent.tinker.android.dx.io.IndexType;
import com.tencent.tinker.android.dx.io.Opcodes;
import com.tencent.tinker.android.dx.io.instructions.InstructionReader;
import com.tencent.tinker.android.dx.io.instructions.InstructionVisitor;
import com.tencent.tinker.android.dx.io.instructions.InstructionWriter;
import com.tencent.tinker.android.dx.io.instructions.ShortArrayCodeInput;
import com.tencent.tinker.android.dx.io.instructions.ShortArrayCodeOutput;
import com.tencent.tinker.commons.dexdifflib.struct.IndexMap;

import java.io.EOFException;

public final class InstructionTransformer {
    private final IndexMap indexMap;

    public InstructionTransformer(IndexMap indexMap) {
        this.indexMap = indexMap;
    }

    public short[] transform(short[] encodedInstructions) throws DexException {
        // encodedInstructions.length * 2 in case we need to change const-string to const-string/jumbo.
        ShortArrayCodeOutput out = new ShortArrayCodeOutput(encodedInstructions.length * 2);
        InstructionWriter iw = new InstructionWriter(out);
        InstructionReader ir = new InstructionReader(new ShortArrayCodeInput(encodedInstructions));

        try {
            ir.accept(new InstructionVisitor(iw) {
                @Override
                public void visitZeroRegisterInsn(int opcode, int index, IndexType indexType, int target, long literal) {
                    int mappedIndex = transformIndexIfNeeded(opcode, index, indexType);
                    super.visitZeroRegisterInsn(opcode, mappedIndex, indexType, target, literal);
                }

                @Override
                public void visitOneRegisterInsn(int opcode, int index, IndexType indexType, int target, long literal, int a) {
                    int mappedIndex = transformIndexIfNeeded(opcode, index, indexType);
                    if (opcode == Opcodes.CONST_STRING && mappedIndex > 0xFFFF) {
                        super.visitOneRegisterInsn(Opcodes.CONST_STRING_JUMBO, index, indexType, target, literal, a);
                    } else {
                        super.visitOneRegisterInsn(opcode, mappedIndex, indexType, target, literal, a);
                    }
                }

                @Override
                public void visitTwoRegisterInsn(int opcode, int index, IndexType indexType, int target, long literal, int a, int b) {
                    int mappedIndex = transformIndexIfNeeded(opcode, index, indexType);
                    super.visitTwoRegisterInsn(opcode, mappedIndex, indexType, target, literal, a, b);
                }

                @Override
                public void visitThreeRegisterInsn(int opcode, int index, IndexType indexType, int target, long literal, int a, int b, int c) {
                    int mappedIndex = transformIndexIfNeeded(opcode, index, indexType);
                    super.visitThreeRegisterInsn(opcode, mappedIndex, indexType, target, literal, a, b, c);
                }

                @Override
                public void visitFourRegisterInsn(int opcode, int index, IndexType indexType, int target, long literal, int a, int b, int c, int d) {
                    int mappedIndex = transformIndexIfNeeded(opcode, index, indexType);
                    super.visitFourRegisterInsn(opcode, mappedIndex, indexType, target, literal, a, b, c, d);
                }

                @Override
                public void visitFiveRegisterInsn(int opcode, int index, IndexType indexType, int target, long literal, int a, int b, int c, int d, int e) {
                    int mappedIndex = transformIndexIfNeeded(opcode, index, indexType);
                    super.visitFiveRegisterInsn(opcode, mappedIndex, indexType, target, literal, a, b, c, d, e);
                }

                @Override
                public void visitRegisterRangeInsn(int opcode, int index, IndexType indexType, int target, long literal, int a, int registerCount) {
                    int mappedIndex = transformIndexIfNeeded(opcode, index, indexType);
                    super.visitRegisterRangeInsn(opcode, mappedIndex, indexType, target, literal, a, registerCount);
                }

                private int transformIndexIfNeeded(int opcode, int index, IndexType indexType) {
                    switch (indexType) {
                        case STRING_REF: {
                            int mappedId = indexMap.adjustStringIndex(index);
                            return mappedId;
                        }
                        case TYPE_REF: {
                            int mappedId = indexMap.adjustTypeIdIndex(index);
                            return mappedId;
                        }
                        case FIELD_REF: {
                            int mappedId = indexMap.adjustFieldIdIndex(index);
                            return mappedId;
                        }
                        case METHOD_REF: {
                            int mappedId = indexMap.adjustMethodIdIndex(index);
                            return mappedId;
                        }
                        default: {
                            return index;
                        }
                    }
                }
            });
        } catch (EOFException e) {
            throw new DexException(e);
        }

        return out.getArray();
    }
}
