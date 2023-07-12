#-------------------------------------------------
# My Terraform module for compute
# Provision:
# - Ubuntu Instance
#
# Made by Anton Nevero. February 2023
#--------------------------------------------------

resource "aws_instance" "myapp_server" {
  ami = data.aws_ami.latest_ubuntu.id
  instance_type = var.instance_type

  subnet_id = var.my-subnet-1
  vpc_security_group_ids = [var.my_sg]
  availability_zone = var.avail_zone

  associate_public_ip_address = true
  key_name = "Ubuntu"
  
  user_data = file("entry-script.sh")

  tags = {
    Name = "${var.env_prefix}-server"
  }
}



