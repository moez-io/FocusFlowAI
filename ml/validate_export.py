#!/usr/bin/env python3
"""Standalone TFLite structural checker; does not claim native LiteRT execution."""
from pathlib import Path
import json
import struct
import numpy as np

BASE=Path(__file__).resolve().parents[1]
D=(BASE/'app/src/main/assets/focusflow.tflite').read_bytes()
W=np.load(BASE/'ml/model_weights.npz')


def u8(p): return D[p]
def u16(p): return struct.unpack_from('<H',D,p)[0]
def u32(p): return struct.unpack_from('<I',D,p)[0]
def i32(p): return struct.unpack_from('<i',D,p)[0]

def field(table,index):
    vtable=table-i32(table)
    length=u16(vtable)
    p=vtable+4+index*2
    if p>=vtable+length: return None
    offset=u16(p)
    return table+offset if offset else None

def ref(table,index):
    f=field(table,index)
    return (f+u32(f)) if f is not None else None

def vector_int(table,index):
    p=ref(table,index)
    return [i32(p+4+4*i) for i in range(u32(p))]

def vector_tables(table,index):
    p=ref(table,index)
    return [p+4+4*i+u32(p+4+4*i) for i in range(u32(p))]

def string(table,index):
    p=ref(table,index)
    return D[p+4:p+4+u32(p)].decode('utf8')

def array_data(table,index):
    p=ref(table,index)
    if p is None:return b''
    return D[p+4:p+4+u32(p)]

root=u32(0)
assert D[4:8]==b'TFL3' and u32(field(root,0))==3
opcodes=vector_tables(root,1)
graphs=vector_tables(root,2)
buffers=vector_tables(root,4)
assert len(opcodes)==2 and len(graphs)==1 and len(buffers)==5
op_vals=[u8(field(c,3)) for c in opcodes]
assert op_vals==[9,25],op_vals  # Fully Connected and Softmax

g=graphs[0]
tensors=vector_tables(g,0)
ops=vector_tables(g,3)
assert len(tensors)==8 and len(ops)==3
assert vector_int(g,1)==[0] and vector_int(g,2)==[7]
shapes=[vector_int(t,0) for t in tensors]
names=[string(t,3) for t in tensors]
assert shapes==[[1,4],[16,4],[16],[1,16],[2,16],[2],[1,2],[1,2]],shapes
assert [u32(field(o,0)) if field(o,0) is not None else 0 for o in ops]==[0,0,1]
assert [vector_int(o,1) for o in ops]==[[0,1,2],[3,4,5],[6]]
assert [vector_int(o,2) for o in ops]==[[3],[6],[7]]
assert [u8(field(o,3)) for o in ops]==[8,8,9]
for tensor_i,buffer_i,param in [(1,1,'w0'),(2,2,'b0'),(4,3,'w1'),(5,4,'b1')]:
    stored=np.frombuffer(array_data(buffers[buffer_i],0),dtype='<f4')
    expected=W[param].ravel()
    np.testing.assert_array_equal(stored,expected)

w0=W['w0']; b0=W['b0']; w1=W['w1']; b1=W['b1']
for raw in [[60,2,10,4],[40,9,0,2],[45,4,8,3],[10,15,0,1],[120,0,15,5]]:
    x=np.array([raw[0]/120,raw[1]/15,raw[2]/25,raw[3]/5],dtype=np.float32)
    hidden=np.maximum(w0@x+b0,0)
    logits=w1@hidden+b1
    p=np.exp(logits-np.max(logits));p=p/np.sum(p)
    assert np.all(np.isfinite(p)) and 0<=p[1]<=1
    print(f'raw={raw} p(need/balanced)={p.tolist()}')
print('PASS: valid TFL3 header, expected graph/tensor shapes, op codes, buffer contents, and finite numpy output')
