# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: yagni.proto

from google.protobuf.internal import enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)




DESCRIPTOR = _descriptor.FileDescriptor(
  name='yagni.proto',
  package='protocol',
  serialized_pb='\n\x0byagni.proto\x12\x08protocol\"f\n\x12PropertyDefinition\x12\x0c\n\x04name\x18\x01 \x02(\t\x12$\n\x04type\x18\x02 \x02(\x0e\x32\x16.protocol.PropertyType\x12\x0c\n\x04unit\x18\x03 \x01(\t\x12\x0e\n\x06symbol\x18\x04 \x01(\t\">\n\x0cResponseType\x12.\n\x08property\x18\x04 \x03(\x0b\x32\x1c.protocol.PropertyDefinition\"E\n\x05\x45vent\x12\x0c\n\x04name\x18\x01 \x02(\t\x12.\n\x08property\x18\x04 \x03(\x0b\x32\x1c.protocol.PropertyDefinition\"s\n\tOperation\x12\x0c\n\x04name\x18\x01 \x02(\t\x12.\n\x08property\x18\x02 \x03(\x0b\x32\x1c.protocol.PropertyDefinition\x12(\n\x08response\x18\x03 \x01(\x0b\x32\x16.protocol.ResponseType\"3\n\x0f\x43ontainedDevice\x12\n\n\x02id\x18\x01 \x02(\t\x12\x14\n\x0c\x64\x65viceTypeId\x18\x02 \x02(\t\"\xd5\x01\n\nDeviceType\x12\n\n\x02id\x18\x01 \x02(\t\x12\x0f\n\x07\x65xtends\x18\x02 \x03(\t\x12\x32\n\x0f\x63ontainedDevice\x18\x03 \x03(\x0b\x32\x19.protocol.ContainedDevice\x12.\n\x08property\x18\x04 \x03(\x0b\x32\x1c.protocol.PropertyDefinition\x12&\n\toperation\x18\x05 \x03(\x0b\x32\x13.protocol.Operation\x12\x1e\n\x05\x65vent\x18\x06 \x03(\x0b\x32\x0f.protocol.Event\"R\n\x08Property\x12\x0c\n\x04name\x18\x01 \x02(\t\x12\r\n\x05value\x18\x02 \x02(\t\x12)\n\x03\x64\x65\x66\x18\x03 \x02(\x0b\x32\x1c.protocol.PropertyDefinition\"m\n\x06\x44\x65vice\x12\n\n\x02id\x18\x01 \x02(\t\x12\x0c\n\x04type\x18\x02 \x02(\t\x12$\n\x08property\x18\x03 \x03(\x0b\x32\x12.protocol.Property\x12#\n\tsubDevice\x18\x04 \x03(\x0b\x32\x10.protocol.Device\"E\n\x0ePropertyUpdate\x12\x10\n\x08\x64\x65viceId\x18\x01 \x02(\t\x12\x12\n\npropertyId\x18\x02 \x02(\t\x12\r\n\x05value\x18\x03 \x02(\t*>\n\x0cPropertyType\x12\x0b\n\x07numeric\x10\x00\x12\n\n\x06string\x10\x01\x12\x0b\n\x07\x62oolean\x10\x02\x12\x08\n\x04json\x10\x03\x42\"\n\x11io.yagni.protocolB\rYagniProtocol')

_PROPERTYTYPE = _descriptor.EnumDescriptor(
  name='PropertyType',
  full_name='protocol.PropertyType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='numeric', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='string', index=1, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='boolean', index=2, number=2,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='json', index=3, number=3,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=916,
  serialized_end=978,
)

PropertyType = enum_type_wrapper.EnumTypeWrapper(_PROPERTYTYPE)
numeric = 0
string = 1
boolean = 2
json = 3



_PROPERTYDEFINITION = _descriptor.Descriptor(
  name='PropertyDefinition',
  full_name='protocol.PropertyDefinition',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='protocol.PropertyDefinition.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='type', full_name='protocol.PropertyDefinition.type', index=1,
      number=2, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='unit', full_name='protocol.PropertyDefinition.unit', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='symbol', full_name='protocol.PropertyDefinition.symbol', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=25,
  serialized_end=127,
)


_RESPONSETYPE = _descriptor.Descriptor(
  name='ResponseType',
  full_name='protocol.ResponseType',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='property', full_name='protocol.ResponseType.property', index=0,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=129,
  serialized_end=191,
)


_EVENT = _descriptor.Descriptor(
  name='Event',
  full_name='protocol.Event',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='protocol.Event.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='property', full_name='protocol.Event.property', index=1,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=193,
  serialized_end=262,
)


_OPERATION = _descriptor.Descriptor(
  name='Operation',
  full_name='protocol.Operation',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='protocol.Operation.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='property', full_name='protocol.Operation.property', index=1,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='response', full_name='protocol.Operation.response', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=264,
  serialized_end=379,
)


_CONTAINEDDEVICE = _descriptor.Descriptor(
  name='ContainedDevice',
  full_name='protocol.ContainedDevice',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='protocol.ContainedDevice.id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='deviceTypeId', full_name='protocol.ContainedDevice.deviceTypeId', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=381,
  serialized_end=432,
)


_DEVICETYPE = _descriptor.Descriptor(
  name='DeviceType',
  full_name='protocol.DeviceType',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='protocol.DeviceType.id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='extends', full_name='protocol.DeviceType.extends', index=1,
      number=2, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='containedDevice', full_name='protocol.DeviceType.containedDevice', index=2,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='property', full_name='protocol.DeviceType.property', index=3,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='operation', full_name='protocol.DeviceType.operation', index=4,
      number=5, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='event', full_name='protocol.DeviceType.event', index=5,
      number=6, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=435,
  serialized_end=648,
)


_PROPERTY = _descriptor.Descriptor(
  name='Property',
  full_name='protocol.Property',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='protocol.Property.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='value', full_name='protocol.Property.value', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='def', full_name='protocol.Property.def', index=2,
      number=3, type=11, cpp_type=10, label=2,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=650,
  serialized_end=732,
)


_DEVICE = _descriptor.Descriptor(
  name='Device',
  full_name='protocol.Device',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='protocol.Device.id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='type', full_name='protocol.Device.type', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='property', full_name='protocol.Device.property', index=2,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='subDevice', full_name='protocol.Device.subDevice', index=3,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=734,
  serialized_end=843,
)


_PROPERTYUPDATE = _descriptor.Descriptor(
  name='PropertyUpdate',
  full_name='protocol.PropertyUpdate',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='deviceId', full_name='protocol.PropertyUpdate.deviceId', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='propertyId', full_name='protocol.PropertyUpdate.propertyId', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='value', full_name='protocol.PropertyUpdate.value', index=2,
      number=3, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=845,
  serialized_end=914,
)

_PROPERTYDEFINITION.fields_by_name['type'].enum_type = _PROPERTYTYPE
_RESPONSETYPE.fields_by_name['property'].message_type = _PROPERTYDEFINITION
_EVENT.fields_by_name['property'].message_type = _PROPERTYDEFINITION
_OPERATION.fields_by_name['property'].message_type = _PROPERTYDEFINITION
_OPERATION.fields_by_name['response'].message_type = _RESPONSETYPE
_DEVICETYPE.fields_by_name['containedDevice'].message_type = _CONTAINEDDEVICE
_DEVICETYPE.fields_by_name['property'].message_type = _PROPERTYDEFINITION
_DEVICETYPE.fields_by_name['operation'].message_type = _OPERATION
_DEVICETYPE.fields_by_name['event'].message_type = _EVENT
_PROPERTY.fields_by_name['def'].message_type = _PROPERTYDEFINITION
_DEVICE.fields_by_name['property'].message_type = _PROPERTY
_DEVICE.fields_by_name['subDevice'].message_type = _DEVICE
DESCRIPTOR.message_types_by_name['PropertyDefinition'] = _PROPERTYDEFINITION
DESCRIPTOR.message_types_by_name['ResponseType'] = _RESPONSETYPE
DESCRIPTOR.message_types_by_name['Event'] = _EVENT
DESCRIPTOR.message_types_by_name['Operation'] = _OPERATION
DESCRIPTOR.message_types_by_name['ContainedDevice'] = _CONTAINEDDEVICE
DESCRIPTOR.message_types_by_name['DeviceType'] = _DEVICETYPE
DESCRIPTOR.message_types_by_name['Property'] = _PROPERTY
DESCRIPTOR.message_types_by_name['Device'] = _DEVICE
DESCRIPTOR.message_types_by_name['PropertyUpdate'] = _PROPERTYUPDATE

class PropertyDefinition(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _PROPERTYDEFINITION

  # @@protoc_insertion_point(class_scope:protocol.PropertyDefinition)

class ResponseType(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _RESPONSETYPE

  # @@protoc_insertion_point(class_scope:protocol.ResponseType)

class Event(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _EVENT

  # @@protoc_insertion_point(class_scope:protocol.Event)

class Operation(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _OPERATION

  # @@protoc_insertion_point(class_scope:protocol.Operation)

class ContainedDevice(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _CONTAINEDDEVICE

  # @@protoc_insertion_point(class_scope:protocol.ContainedDevice)

class DeviceType(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _DEVICETYPE

  # @@protoc_insertion_point(class_scope:protocol.DeviceType)

class Property(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _PROPERTY

  # @@protoc_insertion_point(class_scope:protocol.Property)

class Device(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _DEVICE

  # @@protoc_insertion_point(class_scope:protocol.Device)

class PropertyUpdate(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _PROPERTYUPDATE

  # @@protoc_insertion_point(class_scope:protocol.PropertyUpdate)


DESCRIPTOR.has_options = True
DESCRIPTOR._options = _descriptor._ParseOptions(descriptor_pb2.FileOptions(), '\n\021io.yagni.protocolB\rYagniProtocol')
# @@protoc_insertion_point(module_scope)