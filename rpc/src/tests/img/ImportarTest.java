package tests.img;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import rpc.branch.and.price.Matriz;
import rpg.img.ImportadorImagenes;

class ImportarTest {

	//@Test
	void testImportar() {
		Matriz matriz = ImportadorImagenes.importar(Paths.get("/home/ik/git/rpc/rpc/instancias/1.1.01.tiff"));
		for (int f = 0; f < 5;f++)
			for (int c = 0; c < 5;c++)
				if (f== 1 && c == 0)
					assert(matriz.get(f, c));
				else 
					assert(!matriz.get(f, c));		
	}	

	@Test
	void testBinarizar() throws IOException {
		ImportadorImagenes.binarizarDir("/home/ik/dev/vm shared/transit/textures/textures");	
	}
}
