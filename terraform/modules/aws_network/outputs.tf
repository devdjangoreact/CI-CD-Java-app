output "myapp_sg" {
    value = aws_security_group.myapp_sg.id
}

output "myapp-subnet-1" {
    value = aws_subnet.myapp-subnet-1.id
}