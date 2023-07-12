
module "network" {
    source = "./modules/aws_network"
    env_prefix = "dev"
    vpc_cidr_block = "10.0.0.0/16"
    subnet_cidr_block = "10.0.10.0/24"
    my_ip = "0.0.0.0/0"
    avail_zone = "eu-central-1b"
}

module "compute" {
    source = "./modules/aws_compute"
    avail_zone = "eu-central-1b"
    env_prefix = "dev"
    instance_type = "t2.micro"
    my-subnet-1 = module.network.myapp-subnet-1
    my_sg = module.network.myapp_sg
}