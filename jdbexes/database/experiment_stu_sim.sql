create table experiment_stu_sim
(
	expstuno int not null,
	result nchar(100) null,
  constraint experiment_stu_sim_pk
		primary key (expstuno),
	constraint experiment_stu_sim_experiment_stu_expstuno_fk
		foreign key (expstuno) references experiment_stu (expstuno)
);

create unique index experiment_stu_sim_expstuno_uindex
	on experiment_stu_sim (expstuno);
