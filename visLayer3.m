function visLayer3()

nodes = [ ...
	1.0 0.0 0.0; ...
	0.0 0.0 0.0; ...
	0.6 0.4 1.0; ...
	0.5 0.2 0.0; ...
	0.72 0.27999999999999997 0.7; ...
	0.42 0.27999999999999997 0.7; ...
	0.5066666666666667 0.29333333333333333 0.5666666666666667; ...
	0.7133333333333333 0.18666666666666665 0.4666666666666666; ...
	0.58 0.32 0.7999999999999999; ...
	0.7399999999999999 0.16 0.2333333333333333; ...
	0.6066666666666666 0.29333333333333333 0.5666666666666667; ...
	0.64 0.16 0.2333333333333333; ...
	0.5466666666666666 0.25333333333333335 0.4666666666666666; ...
	0.5 0.06666666666666667 0.0; ...
	0.30666666666666664 0.16 0.2333333333333333; ...
	0.47333333333333333 0.09333333333333332 0.2333333333333333; ...
	0.525 0.15000000000000002 0.25; ...
];


X = linspace(0, 1, 10);
Y = linspace(0, 1, 10);
[ XX, YY ] = meshgrid(X,Y);

disp( XX);
disp( YY);


ZZ1 = ones(10, 10)*0.3;
ZZ2 = ones(10, 10)*0.7;

subplot(1,2,1);
surf(XX,YY, ZZ1, 'FaceAlpha',0.2, 'EdgeColor', [1 1 1], 'LineStyle', '-.', 'FaceColor', 'c', 'FaceLighting', 'flat');

grey = [.25 .25 .25];
scatter3(nodes(:,1),nodes(:,2),nodes(:,3),'filled','MarkerFaceColor',grey)
axis vis3d
#l = line([0.01 0.99], [0.01 0.99], [0.8 0.8], "linestyle", "--", "color", "b");

grid off
box on

s = [1 4 6 3 4 2 1 1 4 3 4 3 ];
e = [6 5 5 4 1 6 2 5 2 6 6 5 ];
n = nan(1,12);



color = [1 0 0];
lx = [nodes(s',1)'; nodes(e',1)'; n];
ly = [nodes(s',2)'; nodes(e',2)'; n];
lz = [nodes(s',3)'; nodes(e',3)'; n];
l = line(lx(:),ly(:),lz(:),'Color',color);

s = [14 16 8 16 15 11 13 10 11 12 8 7 7 14 8 9 14 10 7 12 15 13 10 11 13 9 15 16 9 12 ];
e = [4 1 1 2 2 4 5 5 3 1 5 3 4 2 6 3 1 1 6 6 6 6 4 5 4 5 4 6 6 4 ];
n = nan(1,30);

color = [0.25 0.25 0.25];
lx = [nodes(s',1)'; nodes(e',1)'; n];
ly = [nodes(s',2)'; nodes(e',2)'; n];
lz = [nodes(s',3)'; nodes(e',3)'; n];
l = line(lx(:),ly(:),lz(:),'Color',color);

s = [17 17 17 17 ];
e = [1 2 4 3 ];
n = nan(1,4);

color = [0 1 0];
lx = [nodes(s',1)'; nodes(e',1)'; n];
ly = [nodes(s',2)'; nodes(e',2)'; n];
lz = [nodes(s',3)'; nodes(e',3)'; n];
l = line(lx(:),ly(:),lz(:),'Color',color);

h3 = subplot(1,2,2);
colormap(h3,'winter');
surf(XX,YY, ZZ2, 'FaceAlpha',0.5, 'EdgeColor', [1 1 1], 'LineStyle', '-.', 'FaceColor', 'flat', 'FaceLighting', 'flat');

hold;

surf(XX,YY, ZZ1, 'FaceAlpha',0.2, 'EdgeColor', [1 1 1], 'LineStyle', '-.', 'FaceColor', 'y', 'FaceLighting', 'flat');



grey = [.25 .25 .25];
scatter3(nodes(:,1),nodes(:,2),nodes(:,3),'filled','MarkerFaceColor',grey)
axis vis3d

grid off
box on

s = [1 4 6 3 4 2 1 1 4 3 4 3 ];
e = [6 5 5 4 1 6 2 5 2 6 6 5 ];
n = nan(1,12);



color = [1 0 0];
lx = [nodes(s',1)'; nodes(e',1)'; n];
ly = [nodes(s',2)'; nodes(e',2)'; n];
lz = [nodes(s',3)'; nodes(e',3)'; n];
l = line(lx(:),ly(:),lz(:),'Color',color);

s = [14 16 8 16 15 11 13 10 11 12 8 7 7 14 8 9 14 10 7 12 15 13 10 11 13 9 15 16 9 12 ];
e = [4 1 1 2 2 4 5 5 3 1 5 3 4 2 6 3 1 1 6 6 6 6 4 5 4 5 4 6 6 4 ];
n = nan(1,30);

color = [0.25 0.25 0.25];
lx = [nodes(s',1)'; nodes(e',1)'; n];
ly = [nodes(s',2)'; nodes(e',2)'; n];
lz = [nodes(s',3)'; nodes(e',3)'; n];
l = line(lx(:),ly(:),lz(:),'Color',color);

s = [17 17 17 17 ];
e = [1 2 4 3 ];
n = nan(1,4);

color = [0 1 0];
lx = [nodes(s',1)'; nodes(e',1)'; n];
ly = [nodes(s',2)'; nodes(e',2)'; n];
lz = [nodes(s',3)'; nodes(e',3)'; n];
l = line(lx(:),ly(:),lz(:),'Color',color);

end
