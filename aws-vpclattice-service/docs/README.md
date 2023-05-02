# AWS::VpcLattice::Service

A service is any software application that can run on instances containers, or serverless functions within an account or virtual private cloud (VPC).

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::VpcLattice::Service",
    "Properties" : {
        "<a href="#authtype" title="AuthType">AuthType</a>" : <i>String</i>,
        "<a href="#dnsentry" title="DnsEntry">DnsEntry</a>" : <i><a href="dnsentry.md">DnsEntry</a></i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#certificatearn" title="CertificateArn">CertificateArn</a>" : <i>String</i>,
        "<a href="#customdomainname" title="CustomDomainName">CustomDomainName</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::VpcLattice::Service
Properties:
    <a href="#authtype" title="AuthType">AuthType</a>: <i>String</i>
    <a href="#dnsentry" title="DnsEntry">DnsEntry</a>: <i><a href="dnsentry.md">DnsEntry</a></i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#certificatearn" title="CertificateArn">CertificateArn</a>: <i>String</i>
    <a href="#customdomainname" title="CustomDomainName">CustomDomainName</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### AuthType

_Required_: No

_Type_: String

_Allowed Values_: <code>NONE</code> | <code>AWS_IAM</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DnsEntry

_Required_: No

_Type_: <a href="dnsentry.md">DnsEntry</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

_Required_: No

_Type_: String

_Minimum Length_: <code>3</code>

_Maximum Length_: <code>40</code>

_Pattern_: <code>^(?!svc-)(?![-])(?!.*[-]$)(?!.*[-]{2})[a-z0-9-]+$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### CertificateArn

_Required_: No

_Type_: String

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^(arn(:[a-z0-9]+([.-][a-z0-9]+)*){2}(:([a-z0-9]+([.-][a-z0-9]+)*)?){2}:certificate/[0-9a-z-]+)?$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CustomDomainName

_Required_: No

_Type_: String

_Minimum Length_: <code>3</code>

_Maximum Length_: <code>255</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Arn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Returns the <code>Arn</code> value.

#### CreatedAt

Returns the <code>CreatedAt</code> value.

#### DomainName

Returns the <code>DomainName</code> value.

#### HostedZoneId

Returns the <code>HostedZoneId</code> value.

#### Id

Returns the <code>Id</code> value.

#### LastUpdatedAt

Returns the <code>LastUpdatedAt</code> value.

#### Status

Returns the <code>Status</code> value.

