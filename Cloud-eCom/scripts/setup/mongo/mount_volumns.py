import boto,config
from boto.ec2.connection import EC2Connection

def create_dbs(ec2,size,zone):
	ec2.create_volume(size, zone)

if __name__=="__main__":
	import sys
	if len(sys.argv) < 5:
		print 'usage: mount_volumns.py <filename> <size (GB)> <region> <zone>'
		exit()
	access_key=config.get_aws_credentials(sys.argv[1])
	size=int(sys.argv[2])
	region=sys.argv[3]
	zone=sys.argv[4]
	ec2 = EC2Connection(aws_access_key_id=access_key[0], aws_secret_access_key=access_key[1], region=region)
	create_ebs(ec2,size,zone)
